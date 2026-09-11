package com.cookflow.exception;

/** Transición de estado de pedido no permitida (400). */
public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(Object from, Object to) {
        super("Transición de estado no permitida: " + from + " -> " + to);
    }
}
