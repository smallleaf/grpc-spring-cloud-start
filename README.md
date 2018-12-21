### 本项目让springcloud能支持grpc

### 项目特点
1.springcloud通信协议支持rpc方式(grpc)  
2.grpc客户端可以进行负载均衡
3.grpc拥有注册中心(eureka)

### 如果非springboot项目想使用eureka注册中心，并且能使用grpc，可以参考我另一个项目:[grpc-eureka-springmvc](https://github.com/smallleaf/grpc-eureka-springmvc)

##  使用
1.使用@EnableAutoConfiguration,会自动从该项目的META-INF/spring.factories中加载Grpc服务和客户端自动配置，自动开启配置

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
其中重要的是
grpc.server.port 这里不要改变

3.服务端使用

```$xslt
@GRpcService
public class TagGrpcService extends TagServiceGrpc.TagServiceImplBase {
}


```
使用@GRpcService注解即可引入
服务启动后自动根据配置，即可开启Grpc服务

4.客户端使用

````$xslt
public class GrpcTagService {

    @GrpcClient("THEMESTORE-ACCOUNT")
    private Channel channel;
````

使用@GrpcClient 值是服务名


### 源码解析
源码解析和我另一个项目类似：[grpc-eureka-springmvc](https://github.com/smallleaf/grpc-eureka-springmvc)



## 更新日志
### v1.0.2
1.去掉自动配置功能，将服务于客户端拆开，   
    
开启grpc服务器@EnableGrpcServerAutoConfig    
开启grpc客户端功能@EnableGrpcClientAutoConfig  
