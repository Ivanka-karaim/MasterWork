package com.example.masters.entity.enums;

public enum Type {
    LIGHTING("LIGHTING"),
    CLIMATE("CLIMATE"),
    ENERGY("ENERGY"),
    SENSOR("SENSOR"),
    GRID("GRID");

    private final String value;

    Type(String value) {
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