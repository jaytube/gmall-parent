package com.rtu.gmall.portal.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gmall.pool")
@Data
public class PoolProperties {

    private Integer coreSize;
    private Integer maximumPoolSize;
    private Integer queueSize;
}
