package com.simple.simplerpc.provider;

import com.simple.simplerpc.common.RpcProperties;
import com.simple.simplerpc.enums.ServiceRegistryType;
import com.simple.simplerpc.registry.ServiceRegistry;
import com.simple.simplerpc.registry.ServiceRegistryFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class ProviderConfiguration {

    @Resource
    private RpcProperties properties;

    @Bean
    public Provider initProvider() throws Exception {
        ServiceRegistryType type = ServiceRegistryType.valueOf(properties.getServiceRegistryType());
        ServiceRegistry serviceRegistry = ServiceRegistryFactory.getInstance(type, properties.getServiceRegistryAddress());

        return new Provider(properties.getNettyServerAddress(), serviceRegistry);
    }

}
