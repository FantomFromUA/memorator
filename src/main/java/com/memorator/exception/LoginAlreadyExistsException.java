package com.memorator.exception;

public class LoginAlreadyExistsException extends RuntimeException {
    public LoginAlreadyExistsException() {
        super("Login already in use");
    }
}
