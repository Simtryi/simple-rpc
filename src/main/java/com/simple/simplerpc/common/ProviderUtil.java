package com.simple.simplerpc.common;

public class ProviderUtil {

    /**
     * 构造服务的唯一标识key
     *
     * @param name    服务名
     * @param version 服务版本
     * @return 服务标识
     */
    public static String generateKey(String name, String version) {
        return String.join(":", name, version);
    }

}
