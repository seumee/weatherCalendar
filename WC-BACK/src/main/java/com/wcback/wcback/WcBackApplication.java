package com.wcback.wcback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class WcBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(WcBackApplication.class, args);
    }

}
