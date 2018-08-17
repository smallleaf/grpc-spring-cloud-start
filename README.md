## springcloud 与grpc结合起来

##  使用
1.引入maven即可，自动配置

2.yml配置
```
# grpc端口配置
grpc:
  server:
    port: 8366
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-test.cloud.3g.net.cn/eureka/
    #从Eureka服务端获取注册信息的间隔时间，单位为秒
    registry-fetch-interval-seconds: 30
    fetch-registry: true
    filter-only-up-instances: true
  instance:
    metadata-map:
      ##grpc服务端口这里是固定不变的。
      grpc.server.port: ${grpc.server.port}

```

3.服务端使用

```$xslt
@GRpcService
public class TagGrpcService extends TagServiceGrpc.TagServiceImplBase {
}


```

使用@GRpcService注解即可引入

4.客户端使用

````$xslt
public class GrpcTagService {

    @GrpcClient("THEMESTORE-ACCOUNT")
    private Channel channel;
````

使用@GrpcClient 值是服务名





