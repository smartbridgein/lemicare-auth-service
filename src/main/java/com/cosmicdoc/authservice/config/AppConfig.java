package com.cosmicdoc.authservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.cosmicdoc.common.config.FirebaseConfig;
import com.cosmicdoc.common.config.RepositoryConfig;

/**
 * Main configuration class for auth-service that imports
 * the Firebase and Repository configurations from the common module.
 */
@Configuration
@Import({FirebaseConfig.class, RepositoryConfig.class})
public class AppConfig {
    // This class imports configurations from the common module
    // All beans are inherited from the imported configurations
}
