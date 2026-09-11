package com.cookflow.domain;

/**
 * Marca las entidades sujetas al aislamiento multi-tenant (todas tienen tenant_id).
 * Mañana el tenant_id se resolverá desde el JWT o el header X-Tenant-Id.
 */
public interface TenantScoped {
    Long getTenantId();
    void setTenantId(Long tenantId);
}
