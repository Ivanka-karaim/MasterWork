package com.example.masters.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "✅ Server is running!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}

