package com.myapp.skillswap.exception;

public class InvalidSwapRequestException extends RuntimeException {

    public InvalidSwapRequestException(String message) {
        super(message);
    }
}
