package com.rtu.gmall.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(PoolProperties properties) throws Exception {
        LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(properties.getCoreSize());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(properties.getCoreSize(), properties.getMaximumPoolSize(), 5, TimeUnit.MINUTES, queue);
        return threadPoolExecutor;
    }
}
