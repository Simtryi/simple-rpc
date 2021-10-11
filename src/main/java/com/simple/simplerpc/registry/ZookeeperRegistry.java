package com.simple.simplerpc.registry;

import com.google.common.collect.Lists;
import com.simple.simplerpc.common.ProviderUtil;
import com.simple.simplerpc.common.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper
 */
public class ZookeeperRegistry implements ServiceRegistry {

    private final CuratorFramework client;
    private ServiceDiscovery<Service> serviceDiscovery;
    private List<Closeable> closeableProviders = Lists.newArrayList();

    //本地缓存
    private Map<String, ServiceProvider<Service>> providerMap;
    private final Object lock = new Object();



    public ZookeeperRegistry(String address) throws Exception {
        providerMap = new ConcurrentHashMap<>(256);
        client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
        client.start();

        JsonInstanceSerializer<Service> serializer = new JsonInstanceSerializer<>(Service.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(Service.class)
                .client(client)
                .serializer(serializer)
                .basePath(Constants.BASE_PATH)
                .build();
        serviceDiscovery.start();
    }


    @Override
    public void register(Service service) throws Exception {
        ServiceInstance<Service> serviceInstance = ServiceInstance
                .<Service>builder()
                //使用{服务名}:{服务版本}唯一标识一个服务
                .name(ProviderUtil.generateKey(service.getName(), service.getVersion()))
                .address(service.getHost())
                .port(service.getPort())
                .payload(service)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(Service service) throws Exception {
        ServiceInstance<Service> serviceInstance = ServiceInstance
                .<Service>builder()
                .name(service.getName())
                .address(service.getHost())
                .port(service.getPort())
                .payload(service)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public Service discovery(String name) throws Exception {
        //先读缓存
        ServiceProvider<Service> serviceProvider = providerMap.get(name);
        if (serviceProvider == null) {
            synchronized (lock) {
                serviceProvider = serviceDiscovery
                        .serviceProviderBuilder()
                        .serviceName(name)
                        //设置负载均衡策略，这里使用轮询
                        .providerStrategy(new RoundRobinStrategy<>())
                        .build();

                if(serviceProvider != null) {
                    serviceProvider.start();

                    closeableProviders.add(serviceProvider);
                    providerMap.put(name, serviceProvider);
                }
            }
        }

        ServiceInstance<Service> serviceInstance = serviceProvider.getInstance();
        return serviceInstance == null ? null : serviceInstance.getPayload();
    }

    @Override
    public void close() throws Exception {
        for (Closeable closeable : closeableProviders) {
            CloseableUtils.closeQuietly(closeable);
        }
        serviceDiscovery.close();
    }

}
