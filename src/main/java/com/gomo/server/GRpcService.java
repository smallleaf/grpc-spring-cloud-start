package com.gomo.server;

import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description : rpc服务注解提供
 * @Date : 2018/6/5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GRpcService {
    /**
     * 指定拦截器
     * @return
     */
    Class<? extends ServerInterceptor>[] interceptors() default {};

    boolean applyGlobalInterceptors() default true;
}
