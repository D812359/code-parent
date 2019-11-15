package com.atu.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	
    @RequestMapping("/hello")
    public String index() {
        return "hello world smile!";
    }

    @RequestMapping("/foo")
    public String foo(String foo) {
        return "hello "+foo+"!!";
    }
}