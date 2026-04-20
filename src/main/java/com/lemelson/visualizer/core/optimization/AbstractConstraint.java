package com.lemelson.visualizer.core.optimization;

import java.util.Objects;

public abstract class AbstractConstraint implements Constraint {

    private final String id;
    private final String description;

    protected AbstractConstraint(String id, String description) {
        this.id = requireText(id, "id");
        this.description = requireText(description, "description");
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final String description() {
        return description;
    }

    protected static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
