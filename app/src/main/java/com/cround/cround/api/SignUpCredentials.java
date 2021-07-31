package com.cround.cround.api;

public class SignUpCredentials {
    private String username;
    private String email;
    private String plaintextPassword;

    public SignUpCredentials(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.plaintextPassword = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }
}
