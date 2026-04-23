package com.agg.dumbellcheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String authUri,
        String tokenUri,
        String userInfoUri,
        String scope,
        String frontendSuccessUri,
        String frontendErrorUri,
        boolean secureStateCookie
) {
}
