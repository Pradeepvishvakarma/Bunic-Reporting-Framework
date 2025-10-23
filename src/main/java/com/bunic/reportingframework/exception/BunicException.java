package com.bunic.reportingframework.exception;

public class BunicException extends Exception{

    private static final long serialVersionUID = 1L;

    public BunicException(String message) {
        super(message);
    }

    public BunicException(String message, Throwable cause) {
        super(message, cause);
    }
}
