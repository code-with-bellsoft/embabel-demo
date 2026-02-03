package dev.cyberjar.embabeldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class EmbabelDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbabelDemoApplication.class, args);
    }

}
