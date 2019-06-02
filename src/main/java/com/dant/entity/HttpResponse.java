package com.dant.entity;

import java.io.Serializable;

public class HttpResponse implements Serializable {
    public boolean success;
    public Object response;

    public HttpResponse(boolean success, Object response) {
        this.success = success;
        this.response = response;
    }

    /**
     * Default success constructor. The default value of success is true
     * @param response any object composing the response
     */
    public HttpResponse(Object response) {
        this(true, response);
    }
}
