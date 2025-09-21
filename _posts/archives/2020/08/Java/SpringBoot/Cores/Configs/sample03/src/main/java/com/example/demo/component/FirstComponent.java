package com.example.demo.component;

import com.example.demo.config.FirstConfig;
import com.example.demo.entity.FirstEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirstComponent {
    @Autowired
    FirstConfig firstConfig;

    public void getFirstConfig(){
        FirstEntity firstEntity = firstConfig.getFirstEntity();
        log.info(firstEntity.toString());
    }
}
