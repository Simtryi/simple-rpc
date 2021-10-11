package com.simple.simplerpc.consumer;

import com.simple.simplerpc.enums.ServiceRegistryType;
import com.simple.simplerpc.registry.ServiceRegistryFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author simple
 */
public class ConsumerFactoryBean implements FactoryBean {

    private Object object;

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddress;



    public void init() throws Exception {
        this.object = ConsumerProxy.getProxy(
                interfaceClass,
                serviceVersion,
                ServiceRegistryFactory.getInstance(ServiceRegistryType.valueOf(registryType), registryAddress));
    }


    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }


    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
