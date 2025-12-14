package org.mrp.repository;

import org.mrp.model.Genre;
import org.mrp.model.MediaEntry;
import org.mrp.dto.MediaEntryTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class MediaEntryRepository implements Repository<MediaEntry, MediaEntryTO> {
    public MediaEntryRepository() {
    }

    private UUID insertMediaEntry(MediaEntryTO object) throws SQLException {
        return db.insert(
                "INSERT INTO media_entry (id, title, description, type, release_year, age_restriction, creator_id, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                object.getTitle(),
                object.getDescription(),
                object.getType(),
                object.getReleaseYear(),
                object.getAgeRestriction(),
                object.getCreatorId(),
                new Timestamp(System.currentTimeMillis())
        );
    }

    @Override
    public UUID save(MediaEntryTO object) throws SQLException {
        UUID mediaEntryId = insertMediaEntry(object);

        //Insert Genres in die Zwischentabelle
        for (Genre genre : object.getGenres()) {
            db.insertWithoutUUID(
                    "INSERT INTO media_entry_genre (media_entry_id, genre) VALUES (?, ?)",
                    mediaEntryId,
                    genre
            );
        }

        return mediaEntryId;
    }

    public UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        return db.getUUID(rs, columnName);
    }

    @Override
    public ResultSet findById(UUID id) throws SQLException {
        return db.query("SELECT id, title, description, type, release_year, age_restriction, creator_id, created_at" +
                "FROM media_entry WHERE id = (?)",
                id);
    }

    @Override
    public int delete(UUID id) throws SQLException {
        return db.update("DELETE FROM media_entry WHERE id = ?", id);
    }

    @Override
    public ResultSet findAll() throws SQLException {
        return db.query(
                "SELECT m.*, u.username AS creator_username, STRING_AGG(meg.genre::TEXT, ',') AS genres " +
                        "FROM media_entry m " +
                        "JOIN app_user u ON m.creator_id = u.user_id " +
                        "LEFT JOIN media_entry_genre meg ON m.id = meg.media_entry_id " +
                        "GROUP BY m.id, u.username " +
                        "ORDER BY m.title ASC"
        );
        //STRING_AGG(meg.genre::TEXT, ',') -> comma-seperated String with genre names
    }


    public Object getCreatorObject(UUID mediaEntryId) throws SQLException {
        return db.getValue("SELECT creator_id FROM media_entry WHERE id = ?", mediaEntryId);
    }

    private void updateGenres(UUID mediaEntryId, List<Genre> genres) throws SQLException {
        //Delete all old genres
        db.update("DELETE FROM media_entry_genre WHERE media_entry_id = ?", mediaEntryId);

        //Add new genres
        for (Genre genre : genres) {
            db.insertWithoutUUID(
                    "INSERT INTO media_entry_genre (media_entry_id, genre) VALUES (?, ?)",
                    mediaEntryId,
                    genre
            );
        }
    }

    public int update(MediaEntryTO object) throws SQLException {
        int rowsAffected = db.update(
                "UPDATE media_entry SET title = ?, description = ?, type = ?, " +
                        "release_year = ?, age_restriction = ? WHERE id = ?",
                object.getTitle(),
                object.getDescription(),
                object.getType(),
                object.getReleaseYear(),
                object.getAgeRestriction(),
                object.getId()
        );

        //Update genres
        updateGenres(object.getId(), object.getGenres());

        return rowsAffected;
    }

    //Check if media entry is already in favorites
    public boolean isFavorite(UUID userId, UUID mediaEntryId) throws SQLException {
        Object result = db.getValue(
                "SELECT 1 FROM favorite WHERE user_id = ? AND media_entry_id = ?",
                userId,
                mediaEntryId
        );
        return result != null;
    }

    //Add media entry to favorites
    public UUID addFavorite(UUID userId, UUID mediaEntryId) throws SQLException {
        String sql = "INSERT INTO favorite (id, user_id, media_entry_id) VALUES (?, ?, ?)";
        return db.insert(sql, userId, mediaEntryId); //UUID is generated inside db.insert
    }

    //Remove media entry from favorites
    public int removeFavorite(UUID userId, UUID mediaEntryId) throws SQLException {
        return db.update(
                "DELETE FROM favorite WHERE user_id = ? AND media_entry_id = ?",
                userId,
                mediaEntryId
        );
    }
}
