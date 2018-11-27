package com.gomo.client;

import com.gomo.client.eureka.EurekaNameResolver;
import com.gomo.client.eureka.EurekaNameResolverProvider;
import com.google.common.collect.Lists;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/5
 */
@Slf4j
public class DiscoveryClientChannelFactory implements GrpcChannelFactory {


    private static final String EUREKA = "eureka://";

    private static final String GRPC_PORT = "grpc.server.port";

    private HeartbeatMonitor monitor = new HeartbeatMonitor();

    private DiscoveryClient discoveryClient;

    private List<EurekaNameResolver> discoveryClientNameResolvers = Lists.newArrayList();

    public DiscoveryClientChannelFactory(DiscoveryClient discoveryClient){
        this.discoveryClient = discoveryClient;
    }


    public void addEurekaNameResolverProvider(EurekaNameResolver eurekaNameResolver){
        discoveryClientNameResolvers.add(eurekaNameResolver);
    }

    @EventListener(HeartbeatEvent.class)
    public void heartbeat(HeartbeatEvent event){
        if(this.monitor.update(event.getValue())){
            log.debug("refesh.....");
            for(EurekaNameResolver eurekaNameResolver : discoveryClientNameResolvers){
                eurekaNameResolver.refresh();
            }
        }
    }

    @Override
    public Channel createChannel(String name) {
        return createChannel(name,null);
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(EUREKA+name)
                .nameResolverFactory(new EurekaNameResolverProvider(discoveryClient,GRPC_PORT,this))
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true)
                .build();
        return channel;
    }
}
