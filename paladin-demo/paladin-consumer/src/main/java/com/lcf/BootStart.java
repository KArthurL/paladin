package com.lcf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.lcf"})
public class BootStart {
    public static void main(String[] args) {
        SpringApplication.run(BootStart.class,args);
    }
}
