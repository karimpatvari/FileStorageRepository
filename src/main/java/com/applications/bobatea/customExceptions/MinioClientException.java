package com.applications.bobatea.customExceptions;

public class MinioClientException extends Exception{
    public MinioClientException(String message) {
        super(message);
    }
}
