package com.chargeflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargeFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChargeFlowApplication.class, args);
    }
}
