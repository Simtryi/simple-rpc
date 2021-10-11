package com.simple.simplerpc.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    private String nettyServerAddress;

    private String serviceRegistryAddress;

    private String serviceRegistryType;

}
