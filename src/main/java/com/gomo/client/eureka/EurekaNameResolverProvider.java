package com.gomo.client.eureka;

import com.gomo.client.DiscoveryClientChannelFactory;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * @author : yesheng
 * @Description :
 * @Date : 2018/6/4
 */
public class EurekaNameResolverProvider extends NameResolverProvider {

    protected static final String EUREKA = "eureka";
    private final String portMetaData;

    private DiscoveryClient client;

    private DiscoveryClientChannelFactory discoveryClientChannelFactory;


    public EurekaNameResolverProvider(DiscoveryClient client, String portMetaData,DiscoveryClientChannelFactory discoveryClientChannelFactory) {
        this.client = client;
        this.portMetaData = portMetaData;
        this.discoveryClientChannelFactory = discoveryClientChannelFactory;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 6;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        EurekaNameResolver eurekaNameResolver = new EurekaNameResolver(client, targetUri, portMetaData);
        discoveryClientChannelFactory.addEurekaNameResolverProvider(eurekaNameResolver);
        return eurekaNameResolver;
    }

    @Override
    public String getDefaultScheme() {
        return EUREKA;
    }
}
