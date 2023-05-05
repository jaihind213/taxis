package com.creativefab;

public class LifeCycleException extends Exception{
    public LifeCycleException(String message) {
        super(message);
    }

    public LifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
