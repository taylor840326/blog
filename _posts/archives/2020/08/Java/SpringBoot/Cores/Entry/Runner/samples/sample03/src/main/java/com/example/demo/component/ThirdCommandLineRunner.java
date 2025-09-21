package com.example.demo.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ThirdCommandLineRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(ThirdCommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
    logger.info("这是第三个单独实现的CommandLineRunner");
    }
}
