package com.maybank.demo.dto;

import lombok.Data;

@Data
public class PostResponse {
    private Long userId;
    private Long id;
    private String title;
    private String body;
}
