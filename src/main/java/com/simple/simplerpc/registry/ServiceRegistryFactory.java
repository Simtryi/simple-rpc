package com.simple.simplerpc.registry;

import com.simple.simplerpc.enums.ServiceRegistryType;

public class ServiceRegistryFactory {

    private static volatile ServiceRegistry serviceRegistry;

    public static ServiceRegistry getInstance(ServiceRegistryType type, String address) throws Exception {
        if (serviceRegistry == null) {
            synchronized (ServiceRegistryFactory.class) {
                if (serviceRegistry == null) {
                    if(type == ServiceRegistryType.ZOOKEEPER) {
                        serviceRegistry = new ZookeeperRegistry(address);
                    } else if(type == ServiceRegistryType.EUREKA) {
                        serviceRegistry = new EurekaRegistry(address);
                    }
                }
            }
        }

        return serviceRegistry;
    }

}
