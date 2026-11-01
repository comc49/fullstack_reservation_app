package com.koobrian.reservations.auth;

import org.springframework.stereotype.Component;

// Decides whether a verified Google identity may use the app at all.
// Google proves WHO the person is. This class decides WHETHER they get in.
@Component
public class SignInPolicy {

    private final AuthProperties properties;

    public SignInPolicy(AuthProperties properties) {
        this.properties = properties;
    }

    public boolean isAllowed(GoogleIdentity identity) {
        // YOUR TURN: return true when either rule matches.
        //
        // Rule 1 (production): an allowed domain is configured (not blank),
        //   and the identity's hostedDomain equals it.
        // Rule 2 (development): the identity's email is in the allowed emails list.
        //
        // Useful: properties.allowedDomain(), properties.allowedEmails(),
        //         identity.hostedDomain(), identity.email(),
        //         String.isBlank(), String.equals(), List.contains()
        return properties.allowedEmails().contains(identity.email()) || (!properties.allowedDomain().isBlank() && properties.allowedDomain().equals(identity.hostedDomain()));
    }
}
