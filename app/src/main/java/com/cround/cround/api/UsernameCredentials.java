package com.cround.cround.api;

public class UsernameCredentials {
    private String username;
    private String plaintextPassword;

    public UsernameCredentials(String username, String password) {
        this.username = username;
        this.plaintextPassword = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }
}
