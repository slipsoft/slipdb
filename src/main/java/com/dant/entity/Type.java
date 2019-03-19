package com.dant.entity;

public enum Type {
    invalidData("invalidData"),
    missingData("missingData"),
    internalError("internalErorr");

    private final String fieldDescription;

    Type(String value) {
        fieldDescription = value;
    }
}
