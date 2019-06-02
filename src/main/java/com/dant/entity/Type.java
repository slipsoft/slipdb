package com.dant.entity;

@Deprecated
public enum Type {
    invalidData("invalidData"),
    missingData("missingData"),
    internalError("internalErorr");

    private final String fieldDescription;

    Type(String value) {
        fieldDescription = value;
    }
}
