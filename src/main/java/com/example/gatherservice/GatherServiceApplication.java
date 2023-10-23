package com.example.gatherservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GatherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatherServiceApplication.class, args);
    }

}
