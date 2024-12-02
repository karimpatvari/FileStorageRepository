package com.applications.bobatea.exceptions;

public class UserExistsException extends Exception {
    public UserExistsException() {
        super();
    }

    public UserExistsException(String message) {
        super(message);
    }
}
