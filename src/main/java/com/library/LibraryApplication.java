package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("===============================================");
        System.out.println("  图书馆管理系统后端服务启动成功！");
        System.out.println("  服务地址: http://localhost:8080/api");
        System.out.println("===============================================");
    }
}
