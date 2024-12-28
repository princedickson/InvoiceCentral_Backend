package com.expilicit.InvoiceCentral.Entity;

import java.util.Set;

public enum AppRole {
    USER(Set.of(
            Permission.CUSTOMER_READ,
            Permission.USER_READ
    )),
    ADMIN(Set.of(
            Permission.USER_CREATE,
            Permission.USER_READ,
            Permission.CUSTOMER_READ,
            Permission.USER_UPDATE,
            Permission.CUSTOMER_CREATE,
            Permission.CUSTOMER_UPDATE
    )),
    SYS_ADMIN(Set.of(
            Permission.USER_CREATE,
            Permission.USER_READ,
            Permission.CUSTOMER_READ,
            Permission.USER_UPDATE,
            Permission.CUSTOMER_CREATE,
            Permission.CUSTOMER_UPDATE,
            Permission.CUSTOMER_DELETE,
            Permission.USER_DELETE
    ));
    private final Set<Permission> permissions;

    AppRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
