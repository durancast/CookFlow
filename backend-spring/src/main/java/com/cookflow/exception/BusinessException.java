package com.cookflow.exception;

/** Excepción de negocio con semántica 4xx/5xx. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
