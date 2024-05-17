package com.simplyfelipe.microid.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum RoleName {
    UNDEFINED (0, "UNDEFINED"),
    USER (1, "USER"),
    ADMIN (2, "ADMIN");

    private static final Map<String, RoleName> BY_NAME = new HashMap<>();

    static {
        for (RoleName roleName : values()) {
            BY_NAME.put(roleName.value, roleName);
        }
    }

    public final int code;
    public final String value;

    RoleName(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static RoleName byName(String name) {
        return Optional.ofNullable(name)
                .map(BY_NAME::get)
                .orElse(UNDEFINED);
    }
}
