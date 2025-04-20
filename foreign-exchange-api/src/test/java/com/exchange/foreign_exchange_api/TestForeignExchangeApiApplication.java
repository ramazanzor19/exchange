package com.exchange.foreign_exchange_api;

import org.springframework.boot.SpringApplication;

public class TestForeignExchangeApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ForeignExchangeApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
