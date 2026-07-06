package by.kazachenko.ejka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EjkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EjkaApplication.class, args);
	}

}
