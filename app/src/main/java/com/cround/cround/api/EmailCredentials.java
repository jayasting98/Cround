package com.cround.cround.api;

public class EmailCredentials {
    private String email;
    private String plaintextPassword;

    public EmailCredentials(String email, String password) {
        this.email = email;
        this.plaintextPassword = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }
}
