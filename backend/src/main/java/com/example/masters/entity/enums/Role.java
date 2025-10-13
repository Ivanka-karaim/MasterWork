
package com.example.masters.entity.enums;

public enum Role {
    ADMIN("ADMIN"),
    TEACHER("TEACHER"),
    STUDENT("STUDENT");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

