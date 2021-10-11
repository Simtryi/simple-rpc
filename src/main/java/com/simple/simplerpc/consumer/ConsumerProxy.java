package com.simple.simplerpc.consumer;

import com.alibaba.fastjson.JSON;
import com.simple.simplerpc.network.NettyClient;
import com.simple.simplerpc.network.RpcRequest;
import com.simple.simplerpc.network.RpcResponse;
import com.simple.simplerpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Slf4j
public class ConsumerProxy<T>  implements InvocationHandler {

    private String serviceVersion;
    private ServiceRegistry serviceRegistry;

    public ConsumerProxy(String serviceVersion, ServiceRegistry serviceRegistry) {
        this.serviceVersion = serviceVersion;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 获取代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> interfaceClass, String serviceVersion, ServiceRegistry serviceRegistry) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[] {interfaceClass},
                new ConsumerProxy<>(serviceVersion, serviceRegistry));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //封装请求对象
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        //发送消息
        NettyClient nettyClient = new NettyClient(this.serviceRegistry);
        RpcResponse response = nettyClient.sendRequest(request);

        if (response != null) {
            if(response.getError() != null) {
                throw new RuntimeException("Consumer rpc fail: " + response.getError());
            }

            log.info("Consumer receive response: {}", JSON.toJSON(response));
            return response.getResult();
        } else {
            throw new RuntimeException("Consumer rpc fail: response is null");
        }
    }

}
