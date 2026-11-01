package com.koobrian.reservations.auth;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepository {

    private final JdbcClient jdbcClient;

    public PersonRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Finds the person with this email, or creates them on their first sign-in.
    // Returns their id either way.
    public long upsertByEmail(String email, String fullName) {
        return jdbcClient
            .sql("""
                INSERT INTO people (full_name, email)
                VALUES (coalesce(:fullName, :email), :email)
                ON CONFLICT (email) DO UPDATE SET full_name = EXCLUDED.full_name
                RETURNING id
                """)
            .param("email", email)
            .param("fullName", fullName)
            .query(Long.class)   // the query returns one column: the id
            .single();           // and exactly one row
        }
}