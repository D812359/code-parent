package com.atu.service.impl;

import com.atu.service.TestService;
import org.apache.dubbo.config.annotation.Service;


/**
 * @author: Tom
 * @date: 2020-04-03 19:27
 * @description:
 */
@Service(group = "g1", version = "1.0.0")
public class TestServiceImpl implements TestService {
    @Override
    public String showName() {
        System.out.println("TestService1 -------------------");
        return "HELLO   DUBBO";
    }
}
