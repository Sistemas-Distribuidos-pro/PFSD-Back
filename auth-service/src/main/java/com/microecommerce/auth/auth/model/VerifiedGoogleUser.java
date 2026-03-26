package com.microecommerce.auth.auth.model;

public class VerifiedGoogleUser {

    private final String subject;
    private final String email;
    private final String fullName;

    public VerifiedGoogleUser(String subject, String email, String fullName) {
        this.subject = subject;
        this.email = email;
        this.fullName = fullName;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
