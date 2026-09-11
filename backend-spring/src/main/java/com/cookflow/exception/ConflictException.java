package com.cookflow.exception;

/** Conflicto de unicidad o estado (409). */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
