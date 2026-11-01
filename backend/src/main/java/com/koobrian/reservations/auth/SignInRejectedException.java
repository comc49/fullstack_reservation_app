package com.koobrian.reservations.auth;

// Thrown when a sign-in attempt must be refused. The controller turns it into a 401.
public class SignInRejectedException extends RuntimeException {

    public SignInRejectedException(String message) {
        super(message);
    }
}
