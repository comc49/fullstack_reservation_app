package com.koobrian.reservations.auth;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Everything under "app.auth" in application.properties, read once at startup.
//   app.auth.jwt-secret        -> jwtSecret
//   app.auth.google-client-id  -> googleClientId
//   app.auth.allowed-domain    -> allowedDomain   (production: the church's Workspace domain)
//   app.auth.allowed-emails    -> allowedEmails   (development: a comma separated list)
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    String jwtSecret,
    String googleClientId,
    String allowedDomain,
    List<String> allowedEmails
) {
    // Missing optional settings become empty values instead of null.
    public AuthProperties {
        if (allowedDomain == null) {
            allowedDomain = "";
        }
        if (allowedEmails == null) {
            allowedEmails = List.of();
        }
    }
}
