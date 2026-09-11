package com.cookflow.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException resource(String resource, Object id) {
        return new NotFoundException(resource + " " + id + " no encontrado");
    }
}
