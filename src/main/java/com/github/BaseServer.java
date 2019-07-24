package com.github;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.github.mapper")
public class BaseServer {

    public static void main(String[] args) {
        SpringApplication.run(BaseServer.class,args);
    }
}
