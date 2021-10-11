package com.simple.simplerpc.enums;

import lombok.Getter;

/**
 * @author simple
 *
 * 服务注册中心类型
 */
public enum ServiceRegistryType {

    ZOOKEEPER("ZOOKEEPER"),
    EUREKA("EUREKA"),
    ;

    @Getter
    private String description;

    ServiceRegistryType(String description) {
        this.description = description;
    }

}
