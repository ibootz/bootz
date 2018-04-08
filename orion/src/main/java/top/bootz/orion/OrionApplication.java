package top.bootz.orion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "top.bootz")
public class OrionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrionApplication.class, args);
	}
}
