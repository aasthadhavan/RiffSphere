package com.riffsphere.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        return Map.of(
            "status", "online",
            "message", "Welcome to RiffSphere Backend!",
            "note", "RiffSphere is a Desktop Application (JavaFX). Please check your Taskbar or Alt+Tab to find the application window.",
            "backend_version", "1.0-SNAPSHOT",
            "api_endpoints", Map.of(
                "login", "/api/users/login",
                "h2_console", "/h2-console"
            )
        );
    }
}
