package com.yourdomain.guardianac.checks;

public enum CheckType {
    MOVEMENT("Movement"),
    COMBAT("Combat"),
    PLAYER("Player"),
    EXPLOIT("Exploit");

    private final String name;

    CheckType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
