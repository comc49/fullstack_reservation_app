package com.koobrian.reservations.auth;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

// Checks a Google ID token and returns who it belongs to.
//
// Not a @Bean on purpose: SecurityConfig already defines the JwtDecoder for OUR tokens,
// and a second JwtDecoder bean would confuse Spring about which one checks API requests.
@Component
public class GoogleTokenVerifier {

    // Google publishes the public keys that verify its signatures here.
    private static final String GOOGLE_PUBLIC_KEYS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    // Google uses both spellings for its issuer.
    private static final Set<String> GOOGLE_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final JwtDecoder googleDecoder;

    public GoogleTokenVerifier(AuthProperties properties) {
        String ourClientId = properties.googleClientId();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_PUBLIC_KEYS_URL).build();

        // The signature is checked by the decoder. These are the other rules a token must pass:
        OAuth2TokenValidator<Jwt> rules = new DelegatingOAuth2TokenValidator<>(
            // 1. Not expired.
            new JwtTimestampValidator(),
            // 2. Issued by Google.
            new JwtClaimValidator<Object>(JwtClaimNames.ISS, issuer -> GOOGLE_ISSUERS.contains(String.valueOf(issuer))),
            // 3. Issued FOR OUR APP. Without this, a token meant for some other app would work here.
            new JwtClaimValidator<Object>(JwtClaimNames.AUD, audience ->
                audience instanceof Collection<?> audiences && audiences.contains(ourClientId))
        );
        decoder.setJwtValidator(rules);

        this.googleDecoder = decoder;
    }

    public GoogleIdentity verify(String idToken) {
        Jwt token;
        try {
            token = googleDecoder.decode(idToken);
        } catch (JwtException exception) {
            throw new SignInRejectedException("Invalid Google token");
        }

        String email = token.getClaimAsString("email");
        Boolean emailVerified = token.getClaimAsBoolean("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new SignInRejectedException("Google account email is not verified");
        }

        return new GoogleIdentity(
            email.toLowerCase(Locale.ROOT),
            token.getClaimAsString("name"),
            token.getClaimAsString("hd")
        );
    }
}
