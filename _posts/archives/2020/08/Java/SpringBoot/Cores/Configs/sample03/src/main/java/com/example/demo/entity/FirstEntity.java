package com.example.demo.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@Data
@ToString
public class FirstEntity {
    private String name;
    private String describe;
}
