package com.idcom4.exceptions;

public class IDException extends Exception {

    public IDException(String message) {
        super(message);
    }

    public IDException(Throwable cause) {
        super(cause);
    }


    public IDException(String message, Throwable cause) {
        super(message,  cause);
    }
}
