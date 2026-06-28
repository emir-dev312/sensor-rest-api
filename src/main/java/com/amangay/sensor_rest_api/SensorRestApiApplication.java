package com.amangay.sensor_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate; 

@SpringBootApplication
public class SensorRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorRestApiApplication.class, args);
    }


    @Bean
        public RestTemplate restTemplate() {
        return new RestTemplate();
}
}