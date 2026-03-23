package br.ufc.huwc.gat3;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@RestController
@EnableScheduling
public class Gat3ApiApplication {

	// test
	@GetMapping("/test")
	public String unauthenticatedtest() {
		return "The unauthenticated test worked!";
	}

	@GetMapping("/authenticatedtest")
	public String authenticatedtest() {
		return "The authenticated test worked!";
	}

	public static void main(String[] args) {
		SpringApplication.run(Gat3ApiApplication.class, args);
	}
}
