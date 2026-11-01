package com.koobrian.reservations.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

// Creates OUR access tokens: short lived JWTs signed with app.auth.jwt-secret.
@Component
public class AccessTokenIssuer {

    static final Duration LIFETIME = Duration.ofMinutes(15);

    private final JwtEncoder jwtEncoder;

    public AccessTokenIssuer(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public IssuedAccessToken issue(long personId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(LIFETIME);

        // The claims: what the token says. Anyone can read these, so no secrets in here.
        // Roles and ministries get added here once the roles migration exists.
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("church-ops")
            .subject(String.valueOf(personId))   // "sub": who this token is for
            .claim("email", email)
            .issuedAt(now)                       // "iat"
            .expiresAt(expiresAt)                // "exp": the decoder rejects it after this
            .build();

        // HS256 = HMAC with SHA-256: the same secret signs and verifies.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, expiresAt);
    }
}
