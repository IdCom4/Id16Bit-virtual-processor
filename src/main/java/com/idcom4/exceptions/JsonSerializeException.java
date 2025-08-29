package com.idcom4.exceptions;

public class JsonSerializeException extends IDException {
    public JsonSerializeException(String message, Throwable cause) {
        super(message, cause);
    }
    public JsonSerializeException(Throwable cause) {
        super("Couldn't serialize object to json string", cause);
    }
}
