package com.capstone2025.roadcode.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/test")
    public String hi(){
        return "success";
    }
    @RequestMapping("/run")
    public String run(){
        return "go running";
    }
}
