package com.agg.dumbellcheck.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SecurityProperties.class, CorsProperties.class})
public class ApplicationConfig {
}
