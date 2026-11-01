package com.koobrian.reservations.auth;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    private final JdbcClient jdbcClient;

    public RefreshTokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Saves a new refresh token (its hash, never the token itself).
    public void insert(long personId, String tokenHash, UUID familyId, Instant expiresAt) {
        jdbcClient
            .sql("""
                INSERT INTO refresh_tokens (person_id, family_id, token_hash, expires_at)
                VALUES (:personId, :familyId, :tokenHash, :expiresAt)
                """)
            .param("personId", personId)
            .param("tokenHash", tokenHash)
            .param("familyId", familyId)
            .param("expiresAt", expiresAt.atOffset(ZoneOffset.UTC))       // see the note about Instant
            .update();                           // INSERT returns no rows, so update() instead of query()
    }
}
