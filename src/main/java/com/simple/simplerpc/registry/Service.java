package com.simple.simplerpc.registry;

import lombok.Data;

/**
 * 服务的元数据
 */
@Data
public class Service {

    /**
     * 服务名
     */
    private String name;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 生产者的地址
     */
    private String host;

    /**
     * 服务暴露端口
     */
    private int port;



    public static Service build() {
        return new Service();
    }

    public Service name(String name) {
        this.setName(name);
        return this;
    }

    public Service version(String version) {
        this.setVersion(version);
        return this;
    }

    public Service host(String host) {
        this.setHost(host);
        return this;
    }

    public Service port(int port) {
        this.setPort(port);
        return this;
    }

}
