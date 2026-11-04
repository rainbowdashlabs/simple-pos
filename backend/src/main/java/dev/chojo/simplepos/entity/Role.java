package dev.chojo.simplepos.entity;

/**
 * What a user may do. Each role includes the ones below it: ADMIN manages users and can do everything STAFF can,
 * STAFF works with the whole application, USER can only see their own profile and settings.
 */
public enum Role {
    ADMIN,
    STAFF,
    USER;

    /**
     * The authority Spring Security checks for this role.
     */
    public String authority() {
        return "ROLE_" + name();
    }
}
