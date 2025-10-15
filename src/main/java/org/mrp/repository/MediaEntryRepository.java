package org.mrp.repository;

import org.mrp.database.Database;
import org.mrp.model.MediaEntry;
import org.mrp.dto.MediaEntryTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MediaEntryRepository implements Repository<MediaEntry, MediaEntryTO> {
    private Database db = new Database();

    public MediaEntryRepository() {
    }

    @Override
    public UUID save(MediaEntryTO object) throws SQLException {
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

    //eventuell benötigt
    /*public List<MediaEntry> findByCreator(UUID id) {
        return null;
    }*/
}
