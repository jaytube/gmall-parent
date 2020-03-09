package com.rtu.gmall.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sso.server")
public class SsoConfig {

    private String url;
    private String loginPath;
}
