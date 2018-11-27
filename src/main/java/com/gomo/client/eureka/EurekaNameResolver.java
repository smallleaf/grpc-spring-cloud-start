package com.gomo.client.eureka;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/4
 */
@Slf4j
public class EurekaNameResolver extends NameResolver {


    /**
     * 服务名
     */
    private final String serviceName;

    /**
     * grpc的端口名称
     */
    private final String portMetaData;
    private final DiscoveryClient client;
    private final SharedResourceHolder.Resource<ScheduledExecutorService> timerServiceResource;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;

    @GuardedBy("this")
    private boolean shutdown;

    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    @GuardedBy("this")
    private ScheduledExecutorService timerService;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ScheduledFuture<?> resolutionTask;


    @GuardedBy("this")
    private List<ServiceInstance> serviceInstanceList;


    public EurekaNameResolver(DiscoveryClient client, URI targetUri, String portMetaData) {
        this.portMetaData = portMetaData;
        serviceName = targetUri.getAuthority();
        this.client = client;
        this.timerServiceResource = GrpcUtil.TIMER_SERVICE;
        this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
        this.serviceInstanceList = Lists.newArrayList();

    }

    @Override
    public String getServiceAuthority() {
        return serviceName;
    }

    @Override
    public void start(Listener listener) {
        Preconditions.checkState(this.listener == null, "already started");
        timerService = SharedResourceHolder.get(timerServiceResource);
        this.listener = listener;
        executor = SharedResourceHolder.get(executorResource);
        this.listener = Preconditions.checkNotNull(listener, "listener");
        resolve();
    }


    @Override
    public final synchronized void refresh() {
        if (listener != null) {
            resolve();
        }
    }

    @GuardedBy("this")
    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }
    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (resolutionTask != null) {
            resolutionTask.cancel(false);
        }
        if (timerService != null) {
            timerService = SharedResourceHolder.release(timerServiceResource, timerService);
        }
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }


    private final Runnable resolutionRunnable = new Runnable() {
        @Override
        public void run() {
            Listener savedListener;
            synchronized (EurekaNameResolver.this) {
                // If this task is started by refresh(), there might already be a scheduled task.
                if (resolutionTask != null) {
                    resolutionTask.cancel(false);
                    resolutionTask = null;
                }
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                List<ServiceInstance> newServiceInstanceList;
                try {
                    newServiceInstanceList = client.getInstances(serviceName);
                } catch (Exception e) {
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }

                if (CollectionUtils.isNotEmpty(newServiceInstanceList)) {
                    if (isNeedToUpdateServiceInstanceList(newServiceInstanceList)) {
                        serviceInstanceList = newServiceInstanceList;
                    } else {
                        return;
                    }
                    List<EquivalentAddressGroup> equivalentAddressGroups = Lists.newArrayList();
                    for (ServiceInstance serviceInstance : serviceInstanceList) {
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if (metadata.get(portMetaData) != null) {
                            Integer port = Integer.valueOf(metadata.get(portMetaData));
                            log.info("Found gRPC server {} {}:{}", serviceName, serviceInstance.getHost(), port);
                            EquivalentAddressGroup addressGroup = new EquivalentAddressGroup(new InetSocketAddress(serviceInstance.getHost(), port), Attributes.EMPTY);
                            equivalentAddressGroups.add(addressGroup);
                        } else {
                            log.error("Can not found gRPC server {}", serviceName);
                        }
                    }
                    savedListener.onAddresses(equivalentAddressGroups, Attributes.EMPTY);
                } else {
                    savedListener.onError(Status.UNAVAILABLE.withCause(new RuntimeException("UNAVAILABLE: NameResolver returned an empty list")));
                }
            } finally {
                synchronized (EurekaNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };


    /**
     * 客户端需要判断grpc端口
     * @param newServiceInstanceList
     * @return
     */
    private boolean isNeedToUpdateServiceInstanceList(List<ServiceInstance> newServiceInstanceList) {
        if (serviceInstanceList.size() == newServiceInstanceList.size()) {
            for (ServiceInstance serviceInstance : serviceInstanceList) {
                boolean isSame = false;
                for (ServiceInstance newServiceInstance : newServiceInstanceList) {
                    //判断是否发生了改变  host  grpc的端口
                    if (newServiceInstance.getHost().equals(serviceInstance.getHost()) && newServiceInstance.getPort() ==
                            serviceInstance.getPort()) {
                        Map<String, String> newmetadata = newServiceInstance.getMetadata();
                        Map<String, String> metadata = serviceInstance.getMetadata();
                        if(metadata.get(portMetaData) != null && newmetadata.get(portMetaData) != null &&metadata.get
                                (portMetaData).equals(newmetadata.get(portMetaData))){
                            isSame = true;
                            break;
                        }

                    }
                }
                if (!isSame) {
                    log.info("Ready to update {} server info group list", serviceName);
                    return true;
                }
            }
        } else {
            log.info("Ready to update {} server info group list", serviceName);
            return true;
        }
        return false;
    }

}
