package com.jianbing.excepetions;

public class NetworkException extends RuntimeException {

    public NetworkException() {
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
    public NetworkException(String message) {
        super(message);
    }
}
