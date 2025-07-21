package com.cosmicdoc.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/test")
public class PublicTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from public endpoint!";
    }
}
