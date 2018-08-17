package com.gomo.client;

import io.grpc.ClientInterceptor;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/5
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GrpcClient {

    /**
     * 服务名称
     * @return
     */
    String value();

    Class<? extends ClientInterceptor>[] interceptors() default {};
}
