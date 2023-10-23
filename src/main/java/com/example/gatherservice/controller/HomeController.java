package com.example.gatherservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final Environment env;

    @GetMapping("/health_check")
    public ResponseEntity healthCheck() {
        String port = env.getProperty("local.server.port");
        String message = "It's Working in Gather Servie, port=%s".formatted(port);

        return ResponseEntity.ok().body(message);
    }


}
