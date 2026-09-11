package com.cookflow.exception;

/** Sin tenant resuelto (401/403). */
public class TenantRequiredException extends BusinessException {

    public TenantRequiredException() {
        super("No se pudo resolver el tenant de la request");
    }
}
