package com.eatool.backend.locallogin;

/** Payload from the set-password page: the invite token and the chosen password. */
public class SetPasswordRequest {

    private String token;
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
