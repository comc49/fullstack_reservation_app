package com.koobrian.reservations.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

// Creating and hashing refresh tokens. The token itself goes to the browser cookie;
// only its hash goes to the database.
public final class RefreshTokens {

    // SecureRandom, not Random: Random is predictable, so its "random" tokens could be guessed.
    private static final SecureRandom RANDOM = new SecureRandom();

    private RefreshTokens() {
        // Static helpers only, never instantiated.
    }

    // 32 random bytes (256 bits), as URL safe text so it fits in a cookie.
    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 of the token, as 64 hex characters. Same input, same hash, every time,
    // so the server can find a token by hashing what the cookie sends.
    public static String hash(String token) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            // Every Java runtime must support SHA-256, so this can't happen in practice.
            // Java still makes us handle it, because getInstance declares it (a checked exception).
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
