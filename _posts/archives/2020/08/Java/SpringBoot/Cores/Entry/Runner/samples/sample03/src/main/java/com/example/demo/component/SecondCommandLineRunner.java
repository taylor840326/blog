package com.example.demo.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SecondCommandLineRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(SecondCommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
    logger.info("这是第二个单独实现的CommandLineRunner");
    }
}
