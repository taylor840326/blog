package com.example.demo.config;

import com.example.demo.entity.FirstEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirstConfig {

    @Value("${first.args}")
    private String firstArgs;

    @Bean
    public FirstEntity getFirstEntity(){
        return FirstEntity.builder().name(firstArgs).describe(firstArgs).build();
    }
}
