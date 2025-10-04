package com.game.subwayworkerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SubwayWorkerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubwayWorkerServerApplication.class, args);
    }

}
