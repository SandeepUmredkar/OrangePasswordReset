package com.o2.orange;


public class AccessToken {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private String expires_in;
    private String authentication_time;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getAuthentication_time() {
        return authentication_time;
    }

    public void setAuthentication_time(String authentication_time) {
        this.authentication_time = authentication_time;
    }
}
