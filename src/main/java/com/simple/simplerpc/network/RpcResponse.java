package com.simple.simplerpc.network;

import lombok.Data;

import java.io.Serializable;

/**
 * @author simple
 */
@Data
public class RpcResponse implements Serializable {

    /**
     * 响应Id
     */
    private String requestId;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 返回结果
     */
    private Object result;

}
