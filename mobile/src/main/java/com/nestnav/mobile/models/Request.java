package com.nestnav.mobile.models;

import java.io.Serializable;

public abstract class Request implements Serializable {
     private RequestType type; // Add type field
    public Request(RequestType type) {
        this.type = type;
    }
    public RequestType getRequestType() {
        return type;
    }
}