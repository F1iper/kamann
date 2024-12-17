package pl.kamann.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator/")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Application is up and running!";
    }
}