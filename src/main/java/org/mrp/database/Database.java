package org.mrp.database;

import org.mrp.utils.UUIDGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private final String url;
    private final String user;
    private final String password;

    private Connection connection;

    public Database() {
        this.url = "jdbc:postgresql://localhost:5433/mrp_db";
        this.user = "postgres";
        this.password = "postgres";
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to PostgreSQL database!");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    //Execute query & return ResultSet
    public ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = prepareStatement(sql, params);
        return stmt.executeQuery();
    }

    //Execute update (INSERT, UPDATE, DELETE) & return affected rows
    public int update(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = prepareStatement(sql, params);
        return stmt.executeUpdate();
    }

    //Execute INSERT with pre-generated UUID
    public UUID insert(String sql, Object... params) throws SQLException {
        UUID uuid = UUIDGenerator.generateUUIDv7();

        //Create new params array with UUID as first parameter
        Object[] newParams = new Object[params.length + 1];
        newParams[0] = uuid.toString();
        System.arraycopy(params, 0, newParams, 1, params.length);

        PreparedStatement stmt = prepareStatement(sql, newParams);
        int affectedRows = stmt.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Insert failed, no rows affected.");
        }

        return uuid;
    }

    //Execute INSERT with given parameters (no UUID generation)
    public void insertWithoutUUID(String sql, Object... params) throws SQLException {
        //Prepare  statement with provided parameters
        PreparedStatement stmt = prepareStatement(sql, params);

        //Execute INSERT
        int affectedRows = stmt.executeUpdate();

        //Check if insert was successful
        if (affectedRows == 0) {
            throw new SQLException("Insert failed, no rows affected.");
        }
    }


    //Check if record exists
    public boolean exists(String sql, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            return rs.next();
        }
    }

    //Helper method to create PreparedStatement with parameters safely set
    //-> prevents SQL injection by using parameterized queries instead of string concatenation
    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        setParameters(stmt, params);
        return stmt;
    }

    //Safely binds parameter values to PreparedStatement placeholders (?)
    //Handles special cases like UUID conversion to ensure proper database storage
    /*private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            //Convert UUID to string for database storage
            if (params[i] instanceof UUID) {
                stmt.setString(i + 1, params[i].toString());
            } else {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }*/
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if (param instanceof UUID) {
                stmt.setString(i + 1, param.toString());

            } else if (param instanceof Enum<?>) {
                // Wichtig: PostgreSQL-Enums brauchen expliziten Typ
                stmt.setObject(i + 1, ((Enum<?>) param).name(), java.sql.Types.OTHER);

            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }


    //Helper method to get UUID from ResultSet by column name
    //Safely converts string representation back to UUID object, handling nulls
    public UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        String uuidString = rs.getString(columnName);
        return (uuidString != null) ? UUID.fromString(uuidString) : null;
    }

    //Closes database connection cleanly
    //Always call this when application shuts down to free resources
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }





    //Helper method to get UUID from ResultSet by column index (1-based)
    //Alternative to column name when you know position but not name
    public UUID getUUID(ResultSet rs, int columnIndex) throws SQLException {
        String uuidString = rs.getString(columnIndex);
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

    //Get single value
    public Object getValue(String sql, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        }
    }

    //Get list of values
    public List<Object> getValues(String sql, Object... params) throws SQLException {
        List<Object> values = new ArrayList<>();
        try (ResultSet rs = query(sql, params)) {
            while (rs.next()) {
                values.add(rs.getObject(1));
            }
        }
        return values;
    }

    //Transaction support - begins new transaction by disabling auto-commit
    //Use this when you need multiple operations to succeed or fail as unit
    public void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    //Commits current transaction, making all changes permanent
    //Re-enables auto-commit for future non-transactional operations
    public void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    //Rolls back current transaction, undoing all changes since beginTransaction()
    //Use this in catch blocks when error occurs during transaction
    public void rollback() throws SQLException {
        getConnection().rollback();
        getConnection().setAutoCommit(true);
    }
}
