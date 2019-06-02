package com.dant.entity;

import com.google.gson.Gson;

@Deprecated
public class ResponseError {

    public Location location;
    public Type type;
    public String message;

    public ResponseError (Location location, Type type, String message) {
        this.location = location;
        this.type = type;
        this.message = message;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}