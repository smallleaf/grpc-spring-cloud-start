package com.gomo.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/5
 */
@Configuration
public class GrpcClientAutoConfiguration {


    @Configuration
//  @ConditionalOnBean(annotation = GrpcClient.class)
    @ConditionalOnBean(DiscoveryClient.class)
    protected static class GrpcDisCovery{
        @Bean
        public DiscoveryClientChannelFactory channelFactory(DiscoveryClient discoveryClient){
            return new DiscoveryClientChannelFactory(discoveryClient);
        }

        @Bean
        public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(){
            return new GrpcClientBeanPostProcessor();
        }

    }

}
