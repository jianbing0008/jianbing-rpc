package com.jianbing.excepetions;

public class DiscoveryException extends RuntimeException{
    public DiscoveryException() {
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }

    public DiscoveryException(String message) {
        super(message);
    }
}
