package com.sample.mdintegrationapp.publisher;

import java.io.Serializable;

public class HttpResponse implements Serializable {
    private int responseCode;
    private String response;
    private HttpRequest request;

    protected HttpResponse(HttpRequest request) {
        this.request = request;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int statusCode) {
        this.responseCode = statusCode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public HttpRequest getRequest() {
        return request;
    }
}