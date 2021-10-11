package com.simple.simplerpc.consumer;

import com.simple.simplerpc.common.RpcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class ConsumerConfiguration {

    @Bean
    public static Consumer initConsumer() {
        return new Consumer();
    }

}
