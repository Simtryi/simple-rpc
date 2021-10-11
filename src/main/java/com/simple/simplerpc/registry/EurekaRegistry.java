package com.simple.simplerpc.registry;

public class EurekaRegistry implements ServiceRegistry {

    public EurekaRegistry(String address) {

    }

    @Override
    public void register(Service service) throws Exception {

    }

    @Override
    public void unRegister(Service service) throws Exception {

    }

    @Override
    public Service discovery(String name) throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

}
