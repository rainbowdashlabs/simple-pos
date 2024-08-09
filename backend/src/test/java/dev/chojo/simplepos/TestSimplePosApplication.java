package dev.chojo.simplepos;

import org.springframework.boot.SpringApplication;

public class TestSimplePosApplication {

    public static void main(String[] args) {
        SpringApplication.from(SimplePosApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
