package com.gomo.server;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018-12-21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(GRpcServerAutoConfiguration.class)
public @interface EnableGrpcServerAutoConfig {
}
