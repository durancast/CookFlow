package com.cookflow.domain;

import java.util.Set;

/**
 * Coincide con el enum nativo PostgreSQL `order_status`
 * y modela la transición de estado permitida:
 * pending -&gt; preparing -&gt; served -&gt; paid.
 */
public enum OrderStatus {
    pending, preparing, served, paid;

    public static final Set<OrderStatus> ALLOWED = Set.of(pending, preparing, served, paid);

    /** Valida la transición: solo avanza por el flujo, sin saltos ni retrocesos. */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case pending -> next == preparing;
            case preparing -> next == served;
            case served -> next == paid;
            case paid -> false;
        };
    }
}
