package com.traffic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.traffic.mapper")
public class TrafficWarningApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficWarningApplication.class, args);
    }
}
