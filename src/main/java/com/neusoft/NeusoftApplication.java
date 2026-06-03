package com.neusoft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 这行是关键

@SpringBootApplication
@MapperScan("com.neusoft.mapper")
@EnableScheduling
public class NeusoftApplication {
    public static void main(String[] args) {
        SpringApplication.run(NeusoftApplication.class, args);
    }
}