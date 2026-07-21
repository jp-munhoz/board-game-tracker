package br.com.bg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BoardGameTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardGameTrackerApplication.class, args);
    }
}
