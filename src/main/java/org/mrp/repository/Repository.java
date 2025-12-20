package org.mrp.repository;

import org.mrp.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface Repository<T> {
    Database db = new Database();

    UUID save(T object) throws SQLException;
    ResultSet findById(UUID id) throws SQLException;
    int delete(UUID id) throws SQLException;
    ResultSet findAll() throws SQLException;
}
