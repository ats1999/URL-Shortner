package com.dsabyte.urlshortner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class UrlSortnerApplication {

	public static void main(String[] args) {
		System.out.println("Hello");
		SpringApplication.run(UrlSortnerApplication.class, args);
	}

}
