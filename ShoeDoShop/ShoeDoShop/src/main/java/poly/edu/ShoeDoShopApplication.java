package poly.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShoeDoShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoeDoShopApplication.class, args);
	}

}
