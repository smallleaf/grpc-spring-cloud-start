package com.gomo.server;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : yesheng
 * @Description :  rpc启动时配置加载
 * @Date : 2018/6/5
 */
public class GRpcServerRunner implements CommandLineRunner,DisposableBean {

    private Logger logger = LoggerFactory.getLogger(GRpcServerRunner.class);

    @Autowired
    private AbstractApplicationContext applicationContext;

    @Autowired
    private GRpcServerProperties gRpcServerProperties;

    private Server server;



    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Grpc Server......");

        //获得全局的拦截器
        Collection<ServerInterceptor> globalServerInterceptors = getTypedBeansWithAnnotation(GRpcGlobalInterceptor
                        .class,
                ServerInterceptor.class);

        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(gRpcServerProperties.getPort());

        for(BindableService bindableService: getTypedBeansWithAnnotation(GRpcService.class,BindableService.class)){

            ServerServiceDefinition serviceDefinition = bindableService.bindService();
            GRpcService gRpcServiceAnn = bindableService.getClass().getAnnotation(GRpcService.class);
            if(globalServerInterceptors != null && globalServerInterceptors.size() >0){
                serviceDefinition  = bindInterceptors(serviceDefinition,gRpcServiceAnn,globalServerInterceptors);
            }
            serverBuilder.addService(serviceDefinition);
            logger.info("'{}' service has been registered.", bindableService.getClass().getName());

        }
        server = serverBuilder.build().start();
        logger.info("gRPC Server started, listening on port {}.", gRpcServerProperties.getPort());
        startDaemonAwaitThread();

    }


    /**
     * 守护进程运行
     */
    private void startDaemonAwaitThread() {

        Thread awaitThread  = new Thread(()->{
            try {
                GRpcServerRunner.this.server.awaitTermination();
            } catch (InterruptedException e) {
                logger.error("gRPC server stopped.",e);
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    /**
     * 获得全局的拦截器，和指定的具体拦截器，放在一个集合当中
     * @param serviceDefinition
     * @param gRpcService
     * @param globalServerInterceptors
     * @return
     */
    private  ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GRpcService gRpcService, Collection<ServerInterceptor> globalServerInterceptors) {

        Stream<? extends ServerInterceptor> privateInterceptors = Stream.of(gRpcService.interceptors())
                .map(interceptorClass -> {
                    try {
                        return 0 < applicationContext.getBeanNamesForType(interceptorClass).length ?
                                applicationContext.getBean(interceptorClass) :
                                interceptorClass.newInstance();
                    } catch (Exception e) {
                        throw  new BeanCreationException("Failed to create interceptor instance.",e);
                    }
                });

        List<ServerInterceptor> interceptors = Stream.concat(
                gRpcService.applyGlobalInterceptors() ? globalServerInterceptors.stream(): Stream.empty(),
                privateInterceptors)
                .distinct()
                .collect(Collectors.toList());
        return ServerInterceptors.intercept(serviceDefinition, interceptors);
    }



    private <T> Collection<T> getTypedBeansWithAnnotation(Class<? extends Annotation> annotationType,Class<T> beanType)
            throws Exception{
       return Stream.of(applicationContext.getBeanNamesForType(beanType))
                .filter(name ->{
                    BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    if(beanDefinition.getSource() instanceof StandardMethodMetadata){
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }
                    return null!= applicationContext.getBeanFactory().findAnnotationOnBean(name,annotationType);
                })
                .map(name -> applicationContext.getBean(name,beanType))
                .collect(Collectors.toList());
    }


    @Override
    public void destroy() throws Exception {
        logger.info("Shutting down gRPC server ...");
        Optional.ofNullable(server).ifPresent(Server::shutdown);
        logger.info("gRPC server stopped.");
    }
}
