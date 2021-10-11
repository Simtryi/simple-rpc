package com.simple.simplerpc.network;

import lombok.Data;

import java.io.Serializable;

/**
 * @author simple
 */
@Data
public class RpcRequest implements Serializable {

    /**
     * 请求Id
     */
    private String requestId;

    /**
     * 请求服务版本
     */
    private String serviceVersion;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数
     */
    private Object[] parameters;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

}
