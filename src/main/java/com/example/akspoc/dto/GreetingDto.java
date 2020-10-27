package com.example.akspoc.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class GreetingDto {
    private String msg;
    private Instant time;
}
