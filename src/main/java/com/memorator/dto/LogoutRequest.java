package com.memorator.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}