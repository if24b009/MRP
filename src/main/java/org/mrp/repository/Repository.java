package org.mrp.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface Repository<T, U> {
    UUID save(U object) throws SQLException;
    ResultSet findById(UUID id);
    public void delete(UUID id);
    public ResultSet findAll();
}
