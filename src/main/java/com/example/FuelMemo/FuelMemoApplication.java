package com.example.FuelMemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FuelMemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuelMemoApplication.class, args);
    }

}
