package com.memorator.exception;

public class WordListAlreadyExistsException extends RuntimeException {
    public WordListAlreadyExistsException() {
        super("A word list with this name already exists for this user");
    }
}
