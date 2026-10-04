package com.oudom.marketplace.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oudom.marketplace")
public class MarketplaceApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }
}
