package com.koobrian.reservations.rooms;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

    private final JdbcClient jdbcClient;

    // Spring sees this constructor and passes in a JdbcClient,
    // already connected to the database from application.properties.
    public RoomRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Room> findAll() {
        return jdbcClient
            .sql("""
                SELECT id, name
                FROM rooms
                ORDER BY name
                """)
            .query(Room.class)   // match each row's columns to Room's fields
            .list();
    }
}