package com.gomo.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : yesheng
 * @Description : grpc服务自动配置文件
 * @Date : 2018/6/5
 */
@Configuration
@EnableConfigurationProperties(GRpcServerProperties.class)
public class GRpcServerAutoConfiguration {

    @Bean
    @ConditionalOnBean(annotation = GRpcService.class)
    public GRpcServerRunner gRpcServerRunner(){
        return new GRpcServerRunner();
    }
}
