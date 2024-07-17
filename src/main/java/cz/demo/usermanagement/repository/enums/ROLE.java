package cz.demo.usermanagement.repository.enums;

import lombok.Getter;

/**
 * Roles User can possess
 *
 * Note: Might be too strict to use enums for roles. But usually someone will create it anyway over time.
 */
@Getter
public enum ROLE {
    ROLE_ADMIN("Administrator: Full access"),
    ROLE_USER("User: Read, Edit himself");

    private final String description;

    ROLE(String description) {
        this.description = description;
    }

}
