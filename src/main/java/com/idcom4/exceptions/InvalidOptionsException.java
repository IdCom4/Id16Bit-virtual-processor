package com.idcom4.exceptions;

public class InvalidOptionsException extends IDException {
    public InvalidOptionsException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidOptionsException(Throwable cause) {
        super("Invalid options", cause);
    }
    public InvalidOptionsException(String message) {
        super(message);
    }
}
