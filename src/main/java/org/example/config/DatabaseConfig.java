package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot автоматически настроит DataSource и EntityManagerFactory
    // на основе свойств в application.properties
} 