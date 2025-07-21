package com.cosmicdoc.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CosmicdocAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosmicdocAuthServiceApplication.class, args);
		System.out.println("Welcome to Auth service");
	}

}
