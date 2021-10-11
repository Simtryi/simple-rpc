package com.simple.simplerpc.provider;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.simple.simplerpc.annotation.RpcProvider;
import com.simple.simplerpc.common.ProviderUtil;
import com.simple.simplerpc.common.Constants;
import com.simple.simplerpc.network.NettyServer;
import com.simple.simplerpc.registry.Service;
import com.simple.simplerpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Provider implements BeanPostProcessor, InitializingBean {

    /**
     * NettyServer 启动的地址
     */
    private final String address;

    /**
     * 注册中心
     */
    private final ServiceRegistry serviceRegistry;

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-provider-pool-%d").build();
    private static ThreadPoolExecutor threadPoolExecutor;

    public static Map<String, Object> PROVIDER_MAP = new HashMap<>(256);



    public Provider(String address, ServiceRegistry serviceRegistry) {
        this.address = address;
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 处理 RpcRequest
     */
    public static void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (Provider.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            Constants.PROVIDER_THREAD_POOL_NUM,
                            Constants.PROVIDER_THREAD_POOL_NUM,
                            600L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(Constants.PROVIDER_THREAD_POOL_QUEUE_LEN),
                            threadFactory);
                }
            }
        }

        threadPoolExecutor.submit(task);
    }


    /**
     * 启动 NettyServer
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                NettyServer.start(address);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 1. 将标有 @RpcProvider 注解的 Bean 缓存
     * 2. 注册服务
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //扫描具有 @RpcProvider 注解的 Bean
        RpcProvider providerBean = bean.getClass().getAnnotation(RpcProvider.class);
        if (providerBean == null) {
            return bean;
        }

        //缓存标有 @RpcProvider 注解的 Bean
        String serviceName = providerBean.serviceInterface().getName();
        String serviceVersion = providerBean.serviceVersion();
        String serviceKey = ProviderUtil.generateKey(serviceName, serviceVersion);

        PROVIDER_MAP.put(serviceKey, bean);


        //注册服务到注册中心
        String[] array = address.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        Service service = Service.build()
                .host(host)
                .port(port)
                .name(serviceName)
                .version(serviceVersion);

        try {
            serviceRegistry.register(service);
            log.info("Register service: {}", JSON.toJSON(service));
        } catch (Exception e) {
            log.error("Register service: {} fail, {}", JSON.toJSON(service), e.getMessage());
        }

        return bean;
    }

}
