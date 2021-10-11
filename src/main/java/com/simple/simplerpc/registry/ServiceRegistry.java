package com.simple.simplerpc.registry;

/**
 * 服务注册中心
 *
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     *
     * @param service 服务元数据
     */
    void register(Service service) throws Exception;

    /**
     * 服务注销
     *
     * @param service 服务元数据
     */
    void unRegister(Service service) throws Exception;

    /**
     * 服务发现
     *
     * @param name 服务名
     * @return 服务
     */
    Service discovery(String name) throws Exception;

    /**
     * 关闭
     *
     */
    void close() throws Exception;

}
