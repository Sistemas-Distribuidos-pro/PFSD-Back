package com.microecommerce.auth.auth.model;

public class ExchangeTokenRequest {

    private String provider;
    private String idToken;

    public ExchangeTokenRequest() {
    }

    public ExchangeTokenRequest(String provider, String idToken) {
        this.provider = provider;
        this.idToken = idToken;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
