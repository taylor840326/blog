package com.example.demo.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FirstCommandLineRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(FirstCommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
    logger.info("这是第一个单独实现的CommandLineRunner");
    }
}
