package com.gomo.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : yesheng
 * @Description : grpc属性配置文件
 * @Date : 2018/6/5
 */
@ConfigurationProperties("grpc.server")
public class GRpcServerProperties {

    /**
     * grpc端口
     */
    private int port = 8765;


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
