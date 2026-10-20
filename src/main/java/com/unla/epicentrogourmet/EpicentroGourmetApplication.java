package com.unla.epicentrogourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpicentroGourmetApplication {

	public static void main(String[] args) {
			SpringApplication.run(EpicentroGourmetApplication.class, args);

	}
}