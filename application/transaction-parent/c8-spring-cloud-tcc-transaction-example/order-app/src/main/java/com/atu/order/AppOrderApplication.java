package com.atu.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: Tom
 * @date: 2020-07-22 17:41
 * @description:
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo             //启动dubbo配置与注解
public class AppOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppOrderApplication.class, args);
    }
}