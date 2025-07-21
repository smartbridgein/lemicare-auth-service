package com.cosmicdoc.standalone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

/**
 * Standalone minimal web application for Cloud Run
 * Completely isolated from other components to ensure it works on Cloud Run
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
@RestController
public class MinimalWebApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MinimalWebApplication.class);
        app.setLogStartupInfo(true);
        app.run(args);
        System.out.println("Minimal web application started on port 8080");
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String home() {
        return "Auth service minimal application is running";
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public String health() {
        return "{\"status\":\"UP\"}";
    }
}
