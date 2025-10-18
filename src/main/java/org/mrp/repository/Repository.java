package org.mrp.repository;

import org.mrp.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface Repository<T, U> {
    Database db = new Database();

    UUID save(U object) throws SQLException;
    ResultSet findById(UUID id);
    public int delete(UUID id) throws SQLException;
    public ResultSet findAll() throws SQLException;
}
