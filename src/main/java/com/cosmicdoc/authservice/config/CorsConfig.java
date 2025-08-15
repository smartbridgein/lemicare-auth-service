package com.cosmicdoc.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow specific origins 
        config.addAllowedOrigin("https://healthcare-app-1078740886343.us-central1.run.app"); // Cloud Run frontend US
        config.addAllowedOrigin("https://healthcare-app-145837205370.asia-south1.run.app"); // Cloud Run frontend Asia
        config.addAllowedOrigin("https://healthcare-app-191932434541.asia-south1.run.app"); // Frontend Asia (new)
        config.addAllowedOrigin("https://auth-service-145837205370.asia-south1.run.app"); // Auth service Asia
        config.addAllowedOrigin("https://auth-service-1078740886343.us-central1.run.app"); // Auth service US
        config.addAllowedOrigin("https://auth-service-191932434541.asia-south1.run.app"); // Auth service Asia (new)

        config.addAllowedOrigin("http://localhost:4200"); // Local development
        // Allow all HTTP methods
        config.addAllowedMethod("*");
        
        // Allow all headers
        config.addAllowedHeader("*");
        
        // Allow credentials (cookies, authorization headers, etc.)
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Apply this configuration to all paths
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
