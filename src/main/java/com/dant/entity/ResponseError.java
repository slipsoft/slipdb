package com.dant.entity;

public class ResponseError {

    public Location location;
    public Type type;
    public String message;

    public ResponseError (Location location, Type type, String message) {
        this.location = location;
        this.type = type;
        this.message = message;
    }

}