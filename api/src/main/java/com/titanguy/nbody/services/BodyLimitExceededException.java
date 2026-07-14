package com.titanguy.nbody.services;

public class BodyLimitExceededException extends Exception {
    public BodyLimitExceededException(String message) {
        super(message);
    }
}
