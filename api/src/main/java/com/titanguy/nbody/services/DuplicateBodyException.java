package com.titanguy.nbody.services;

public class DuplicateBodyException extends Exception {
    public DuplicateBodyException(String message) {
        super(message);
    }
}
