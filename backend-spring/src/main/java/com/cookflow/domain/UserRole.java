package com.cookflow.domain;

/**
 * Debe coincidir con los miembros del enum nativo PostgreSQL `user_role`
 * ('admin','waiter','kitchen','manager') para que @Enumerated(EnumType.STRING) funcione.
 */
public enum UserRole {
    admin, waiter, kitchen, manager
}
