package com.koobrian.reservations.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // What the frontend sends: the ID token Google gave the browser.
    public record GoogleSignInRequest(String idToken) {
    }

    // What the frontend gets back in the body. The refresh token is NOT here: it goes in the cookie.
    public record AccessTokenResponse(String accessToken, Instant expiresAt) {
    }

    // POST /v1/auth/google   body: { "idToken": "eyJhbGciOiJSUzI1NiIs..." }
    @PostMapping("/v1/auth/google")
    public ResponseEntity<AccessTokenResponse> signInWithGoogle(@RequestBody GoogleSignInRequest request) {
        if (request.idToken() == null || request.idToken().isBlank()) {
            throw new SignInRejectedException("Missing idToken");
        }

        SignInResult result = authService.signInWithGoogle(request.idToken());

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, result.refreshToken())
            .httpOnly(________)              // blank A: can JavaScript read it?
            .secure(________)                // blank B: HTTPS only?
            .sameSite("________")            // blank C: sent when another site starts the request?
            .path("/v1/auth")                // only sent to /v1/auth/... (refresh and logout)
            .maxAge(Duration.between(Instant.now(), result.refreshExpiresAt()))
            .build();

        AccessTokenResponse body = new AccessTokenResponse(
            result.accessToken().token(),
            result.accessToken().expiresAt()
        );

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(body);
    }

    // Any SignInRejectedException thrown while handling a request in this controller becomes a 401.
    @ExceptionHandler(SignInRejectedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleRejectedSignIn() {
        // The status is enough. The reason stays in the server logs, not in the response,
        // so an attacker can't learn which check failed.
    }
}
