package com.gomo.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description : rpc拦截器
 * @Date : 2018/6/5
 */
@Target({ElementType.TYPE,ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GRpcGlobalInterceptor {

}
