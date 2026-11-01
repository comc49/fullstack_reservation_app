package com.koobrian.reservations.auth;

import java.time.Instant;

// A freshly signed access token and the moment it stops working.
public record IssuedAccessToken(
    String token,
    Instant expiresAt
) {
}
