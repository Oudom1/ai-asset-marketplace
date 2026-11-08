package com.oudom.marketplace.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] origins;

    public CorsConfig(@Value("${marketplace.cors-origins:https://oudom1.github.io,https://ai-asset-marketplace.onrender.com}") String origins) {
        this.origins = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(origins)
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "X-Buyer-Email")
            .maxAge(3600);
    }
}
