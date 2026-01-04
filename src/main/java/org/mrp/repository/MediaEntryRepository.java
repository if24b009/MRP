package org.mrp.repository;

import org.mrp.model.Genre;
import org.mrp.model.MediaEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MediaEntryRepository implements Repository<MediaEntry> {
    public MediaEntryRepository() {
    }

    private UUID insertMediaEntry(MediaEntry object) throws SQLException {
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
    public UUID save(MediaEntry object) throws SQLException {
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
        return db.query("SELECT m.*" +
                        "COALESCE(AVG(r.stars_ct), 0) AS avg_rating" +
                        "FROM media_entry m " +
                        "LEFT JOIN rating r ON m.id = r.media_entry_id " +
                        "WHERE m.id = ? " +
                        "GROUP BY m.id, m.creator_id",
                id);
    }

    @Override
    public int delete(UUID id) throws SQLException {
        return db.update("DELETE FROM media_entry WHERE id = ?", id);
    }

    //findAll() without Filters & Sort
    @Override
    public ResultSet findAll() throws SQLException {
        return db.query(
                "SELECT m.*, u.username AS creator_username, " +
                        "COALESCE(AVG(r.stars_ct), 0) AS avg_rating, " +
                        "COUNT(DISTINCT r.id) AS total_ratings, " +
                        "STRING_AGG(meg.genre::TEXT, ',') AS genres " +
                        "FROM media_entry m " +
                        "JOIN app_user u ON m.creator_id = u.user_id " +
                        "LEFT JOIN media_entry_genre meg ON m.id = meg.media_entry_id " +
                        "LEFT JOIN rating r ON m.id = r.media_entry_id " +
                        "GROUP BY m.id, u.username " +
                        "ORDER BY m.title ASC"
        );
        //STRING_AGG(meg.genre::TEXT, ',') -> comma-seperated String with genre names
    }

    //findAll() with Filters & Sort
    public ResultSet findAll(Map<String, String> filters, String sortBy) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT me.*, u.username AS creator_username, " +
                        "COALESCE(AVG(r.stars_ct), 0) AS avg_rating, " +
                        "COUNT(DISTINCT r.id) AS total_ratings, " +
                        "STRING_AGG(meg.genre::TEXT, ',') AS genres " +
                        "FROM media_entry me " +
                        "JOIN app_user u ON me.creator_id = u.user_id " +
                        "LEFT JOIN media_entry_genre meg ON me.id = meg.media_entry_id " +
                        "LEFT JOIN rating r ON me.id = r.media_entry_id " +
                        "WHERE 1=1 "
        );
        //WHERE 1=1 -> always true -> to append starting with AND

        List<Object> queryParams = new ArrayList<>();

        //Filters
        applyFilters(filters, sql, queryParams);

        //Sorting
        applySorting(sortBy, sql);

        return db.query(sql.toString(), queryParams.toArray());
    }

    private void applyFilters(Map<String, String> filters, StringBuilder sql, List<Object> queryParams) {
        if (filters == null) return;

        //Search by title
        String search = filters.get("search");
        if (search != null && !search.isEmpty()) {
            sql.append("AND LOWER(me.title) LIKE LOWER(?) ");
            queryParams.add("%" + search + "%");
        }

        //Media Entry Type
        String type = filters.get("type");
        if (type != null && !type.isEmpty()) {
            sql.append("AND me.type = ? ");
            queryParams.add(type.toUpperCase());
        }

        //Genre
        String genre = filters.get("genre");
        if (genre != null && !genre.isEmpty()) {
            sql.append(
                    "AND me.id IN (SELECT media_entry_id FROM media_entry_genre WHERE genre = ?) "
            );
            queryParams.add(genre.toUpperCase());
        }

        //Release year
        String releaseYear = filters.get("releaseYear");
        if (releaseYear != null && !releaseYear.isEmpty()) {
            sql.append("AND me.release_year = ? ");
            queryParams.add(Integer.parseInt(releaseYear));
        }

        //Age restriction
        String ageRestriction = filters.get("ageRestriction");
        if (ageRestriction != null && !ageRestriction.isEmpty()) {
            sql.append("AND me.age_restriction = ? ");
            queryParams.add(Integer.parseInt(ageRestriction));
        }

        //Grouping for aggregates
        sql.append("GROUP BY me.id, u.username ");

        //Rating (HAVING)
        String ratingStr = filters.get("rating");
        if (ratingStr != null && !ratingStr.isEmpty()) {
            sql.append("HAVING COALESCE(AVG(r.stars_ct), 0) >= ? ");
            queryParams.add(Double.parseDouble(ratingStr));
        }
    }
    private void applySorting(String sortBy, StringBuilder sql) {
        if ("year".equals(sortBy)) {
            sql.append("ORDER BY me.release_year DESC");
        } else if ("rating".equals(sortBy)) {
            sql.append("ORDER BY avg_rating DESC");
        } else {
            sql.append("ORDER BY me.title ASC");
        }
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

    public int update(MediaEntry object) throws SQLException {
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


    //get Timestamp created_at
    public ResultSet getCreated_at(UUID id) throws SQLException {
        return db.query("SELECT created_at FROM media_entry WHERE id = ?", id);
    }
}
