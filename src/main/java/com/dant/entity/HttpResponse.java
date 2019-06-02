package com.dant.entity;

import java.io.Serializable;

public class HttpResponse implements Serializable {
    public String message;
    public Object response;

    public HttpResponse(String message, Object response) {
        this.message = message;
        this.response = response;
    }
}
