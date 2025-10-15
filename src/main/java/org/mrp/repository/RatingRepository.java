package org.mrp.repository;

import org.mrp.database.Database;
import org.mrp.model.Rating;
import org.mrp.dto.RatingTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RatingRepository implements Repository<Rating, RatingTO> {
    private Database db = new Database();

    public RatingRepository() {
    }

    @Override
    public UUID save(RatingTO object) throws SQLException {
        return null;
    }

    @Override
    public ResultSet findById(UUID id) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public ResultSet findAll() {
        return null;
    }

    /*
    //eventuell benötigt
    public List<Rating> findByMediaEntryId(UUID id) {
        return null;
    }

    public List<Rating> findByUser(UUID id) {
        return null;
    }

    public List<Rating> findByUserAndMediaEntry(UUID userId, UUID mediaEntryId) {
        return null;
    }
    */
}
