package com.oudom.marketplace.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
            "service", "AI Asset Marketplace API",
            "status", "UP",
            "health", "/api/health",
            "assets", "/api/assets"
        );
    }
}
