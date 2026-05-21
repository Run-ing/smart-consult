package com.example.smartconsult;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.smartconsult.**.mapper")
public class SmartConsultApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartConsultApplication.class, args);
    }
}
