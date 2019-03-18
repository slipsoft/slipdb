package com.dant.entity;

import java.io.Serializable;

public class HttpResponse implements Serializable {

    private String status;
    private String message;
    private Object response;

    public HttpResponse(String status, String message, Object response) {
        this.status = status;
        this.message = message;
        this.response = response;
    }
}
