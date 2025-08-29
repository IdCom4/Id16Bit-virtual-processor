package com.idcom4.exceptions;

public class JsonDeserializeException extends IDException {
    public JsonDeserializeException(String message, Throwable cause) {
        super(message, cause);
    }
    public JsonDeserializeException(Throwable cause) {
        super("Couldn't deserialize json string", cause);
    }
}
