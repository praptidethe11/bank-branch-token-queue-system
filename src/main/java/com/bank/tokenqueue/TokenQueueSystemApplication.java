package com.bank.tokenqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TokenQueueSystemApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TokenQueueSystemApplication.class, args);
    }

    // This override allows the app to also be deployed as a WAR
    // to an external Tomcat server (needed for Task 8).
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TokenQueueSystemApplication.class);
    }
}
