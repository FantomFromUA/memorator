package com.memorator.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordListResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private int wordCount;
}
