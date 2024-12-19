package com.applications.bobatea.customExceptions;

public class UserExistsException extends Exception {
    public UserExistsException() {
        super();
    }

    public UserExistsException(String message) {
        super(message);
    }
}
