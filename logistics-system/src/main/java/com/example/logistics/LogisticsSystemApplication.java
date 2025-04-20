package com.example.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.example.logistics", "com.example.common"})
public class LogisticsSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticsSystemApplication.class, args);
    }
}