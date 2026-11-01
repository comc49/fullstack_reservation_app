package com.koobrian.reservations.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    static final Duration REFRESH_LIFETIME = Duration.ofDays(30);

    private final GoogleTokenVerifier googleTokenVerifier;
    private final SignInPolicy signInPolicy;
    private final PersonRepository personRepository;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
        GoogleTokenVerifier googleTokenVerifier,
        SignInPolicy signInPolicy,
        PersonRepository personRepository,
        AccessTokenIssuer accessTokenIssuer,
        RefreshTokenRepository refreshTokenRepository
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.signInPolicy = signInPolicy;
        this.personRepository = personRepository;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Steps 3 to 5 of the flow: verify Google's token, find or create the person,
    // then issue our access token and start a new refresh token family.
    public SignInResult signInWithGoogle(String googleIdToken) {
        // 1. Who is this? (Throws SignInRejectedException if the token is bad.)
        GoogleIdentity identity = googleTokenVerifier.verify(googleIdToken);

        // 2. May they use the app at all?
        if (!this.signInPolicy.isAllowed(identity)) {
            throw new SignInRejectedException("This account is not allowed to sign in");
        }

        // 3. Find or create them in our database.
        long personId = this.personRepository.upsertByEmail(identity.email(), identity.fullName());

        // 4. Our access token.
        IssuedAccessToken accessToken = this.accessTokenIssuer.issue(personId, identity.email());

        // 5. A new refresh token, in a new family: this sign-in starts a new chain.
        String refreshToken = RefreshTokens.newToken();
        UUID familyId = UUID.randomUUID();
        Instant refreshExpiresAt = Instant.now().plus(REFRESH_LIFETIME);

        // 6. Save its HASH, never the token itself.
        refreshTokenRepository.insert(personId, RefreshTokens.hash(refreshToken), familyId, refreshExpiresAt);

        return new SignInResult(accessToken, refreshToken, refreshExpiresAt);
    }
}
