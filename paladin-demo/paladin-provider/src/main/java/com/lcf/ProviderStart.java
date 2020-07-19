package com.lcf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.Scanner;

@SpringBootApplication(scanBasePackages = {"com.lcf"})
public class ProviderStart {
    public static void main(String[] args)  {
        SpringApplication.run(ProviderStart.class,args);

    }
}
