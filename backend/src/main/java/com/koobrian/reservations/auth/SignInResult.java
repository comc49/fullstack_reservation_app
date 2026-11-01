package com.koobrian.reservations.auth;

import java.time.Instant;

// Everything the controller needs after a successful sign-in:
// the access token for the response body, and the refresh token for the cookie.
public record SignInResult(
    IssuedAccessToken accessToken,
    String refreshToken,
    Instant refreshExpiresAt
) {
}
