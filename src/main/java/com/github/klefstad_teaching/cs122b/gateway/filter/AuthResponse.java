package com.github.klefstad_teaching.cs122b.gateway.filter;

public class AuthResponse {

    private MyResultClass result;
    private String accessToken;

    public MyResultClass getResult() {
        return result;
    }

    public void setResult(MyResultClass result) {
        this.result = result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AuthResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
}
