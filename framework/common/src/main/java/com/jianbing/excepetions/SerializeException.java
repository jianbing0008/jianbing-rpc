package com.jianbing.excepetions;

public class SerializeException extends RuntimeException{
    public SerializeException() {
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException(String message) {
        super(message);
    }
}
