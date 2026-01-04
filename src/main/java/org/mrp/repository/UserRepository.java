package org.mrp.repository;

import org.mrp.model.Genre;
import org.mrp.model.MediaEntry;
import org.mrp.model.MediaEntryType;
import org.mrp.model.User;
import org.mrp.service.MediaEntryService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepository implements Repository<User> {
    private MediaEntryService mediaEntryService = new MediaEntryService();

    public UserRepository() {
    }

    //for registration (AuthService)
    @Override
    public UUID save(User user) throws SQLException {
        return db.insert(
                "INSERT INTO app_user (user_id, username, password_hashed) VALUES (?, ?, ?)",
                user.getUsername(),
                user.getPassword_hashed()
        );
    }

    //for login (AuthService) -> Token
    public void update(String token, UUID userId) throws SQLException {
        db.update(
                "INSERT INTO token (token, user_id, created_at) VALUES (?, ?, ?) ON CONFLICT (user_id) DO UPDATE SET token = (?), created_at = (?)",
                token,
                userId,
                new Timestamp(System.currentTimeMillis()),
                token,
                new Timestamp(System.currentTimeMillis())
        );
    }

    public UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        return db.getUUID(rs, columnName);
    }

    @Override
    public ResultSet findById(UUID id) {
        return null;
    }

    public ResultSet findByUsername(String username) throws SQLException {
        return db.query(
                "SELECT user_id, username, password_hashed FROM app_user WHERE username = ?",
                username
        );
    }

    public ResultSet findByToken(String token) throws SQLException {
        return db.query(
                "SELECT user_id FROM token WHERE token = ?",
                token
        );
    }

    @Override
    public ResultSet findAll() {
        return null;
    }

    @Override
    public int delete(UUID id) {
        return 0;
    }

    public boolean userAlreadyExists(String username) throws SQLException {
        return db.exists("SELECT * FROM app_user WHERE username = ?", username);
    }

    public ResultSet getFavoriteMediaEntries(UUID userId) throws SQLException {
        return db.query(
                "SELECT m.*, " +
                        "STRING_AGG(meg.genre::TEXT, ',') AS genres, " +
                        "u.username AS creator_username, " +
                        "COALESCE(AVG(r.stars_ct), 0) AS avg_rating, " +
                        "COUNT(DISTINCT r.id) AS total_ratings " +
                        "FROM media_entry m " +
                        "INNER JOIN favorite f ON m.id = f.media_entry_id " +
                        "INNER JOIN app_user u ON m.creator_id = u.user_id " +
                        "LEFT JOIN rating r ON m.id = r.media_entry_id " +
                        "LEFT JOIN media_entry_genre meg ON m.id = meg.media_entry_id " +
                        "WHERE f.user_id = ? " +
                        "GROUP BY m.id, u.username " +
                        "ORDER BY m.title ASC",
                userId
        );
    }

    public ResultSet getLeaderboard() throws SQLException {
        return db.query(
                "SELECT u.username, " +
                        "(SELECT COUNT(*) FROM rating r WHERE r.user_id = u.user_id) AS rating_count, " +
                        "(SELECT COUNT(*) FROM media_entry m WHERE m.creator_id = u.user_id) AS media_created, " +
                        "(SELECT COUNT(*) FROM rating_likes rl WHERE rl.user_id = u.user_id) AS likes_given " +
                        "FROM app_user u " +
                        "ORDER BY " +
                        "(SELECT COUNT(*) FROM rating r WHERE r.user_id = u.user_id) + " +
                        "(SELECT COUNT(*) FROM media_entry m WHERE m.creator_id = u.user_id) + " +
                        "(SELECT COUNT(*) FROM rating_likes rl WHERE rl.user_id = u.user_id) DESC " +
                        "LIMIT 10"
        );
    }

    public boolean isExistingUsername(String username) throws SQLException {
        return db.exists("SELECT 1 FROM app_user WHERE username = ?", username);
    }

    public void updateUsername(UUID userId, String username) throws SQLException {
        db.update(
                "UPDATE app_user SET username = ? WHERE user_id = ?",
                username, userId
        );
    }


    //For User Statistics (Profile)

    public int getRatings_total(UUID userId) throws SQLException {
        Object ct = db.getValue(
                "SELECT COUNT(*) FROM rating WHERE user_id = ?",
                userId
        );
        return ct != null ? ((Number) ct).intValue() : 0;
    }

    public int getFavorites_total(UUID userId) throws SQLException {
        Object ct = db.getValue(
                "SELECT COUNT(*) FROM favorite WHERE user_id = ?",
                userId
        );
        return ct != null ? ((Number) ct).intValue() : 0;
    }

    public int getMediaEntriesCreated_total(UUID userId) throws SQLException {
        Object ct = db.getValue(
                "SELECT COUNT(*) FROM media_entry WHERE creator_id = ?",
                userId
        );
        return ct != null ? ((Number) ct).intValue() : 0;
    }

    public double getAvgScore(UUID userId) throws SQLException {
        Object avg = db.getValue(
                "SELECT COALESCE(AVG(stars_ct), 0) FROM rating WHERE  user_id = ?",
                userId
        );
        return avg != null ? ((Number) avg).doubleValue() : 0.0;
    }


    //Recommendations

    //rated from user
    public ResultSet getUserTopRatedMediaEntries(UUID userId) throws SQLException {
        return db.query(
                "SELECT " +
                        "me.id, " +
                        "me.type, " +
                        "me.age_restriction, " +
                        "STRING_AGG(meg.genre::TEXT, ',') AS genres " +
                        "FROM rating r " +
                        "JOIN media_entry me ON r.media_entry_id = me.id " +
                        "JOIN media_entry_genre meg ON me.id = meg.media_entry_id " +
                        "WHERE r.user_id = ? AND r.stars_ct >= 4 " +
                        "GROUP BY me.id",
                userId
        );
    }

    public List<MediaEntry> findRecommendations(UUID userId, List<Genre> favoriteGenres, List<MediaEntryType> favoriteMediaTypes, List<Integer> preferredAgeRestrictions) throws SQLException {
        if (favoriteGenres.isEmpty() && favoriteMediaTypes.isEmpty() && preferredAgeRestrictions.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder filterConditions = new StringBuilder(); //StringBuilder = efficient way to build string
        //filterConditions = added WHERE-Klausel
        List<Object> filterParams = new ArrayList<>(); //List -> unsure how many parameters

        //Exclude already from user rated media entries
        filterParams.add(userId);

        //Genre: Build Genre-WHERE-Klausel
        if (!favoriteGenres.isEmpty()) appendINClause("meg.genre", favoriteGenres, filterConditions, filterParams);

        //Media Entry Type: Build MediaType-WHERE-Klausel
        if (!favoriteMediaTypes.isEmpty()) appendINClause("m.type", favoriteMediaTypes, filterConditions, filterParams);

        //Age Restriction: Build AgeRestriction-WHERE-Klausel
        if (!preferredAgeRestrictions.isEmpty()) appendINClause("m.age_restriction", preferredAgeRestrictions, filterConditions, filterParams);

        ResultSet rs = getRecommendationsBasedOnConditions(filterConditions, filterParams);

        List<MediaEntry> recommendations = new ArrayList<>();
        while (rs.next()) {
            recommendations.add(mediaEntryService.mapResultSetToMediaEntry(rs));
        }
        return recommendations;
    }

    private ResultSet getRecommendationsBasedOnConditions(StringBuilder conditions, List<Object> params) throws SQLException {
        return db.query("SELECT m.*, u.username AS creator_username, " +
                "COALESCE(AVG(r.stars_ct), 0) AS avg_rating, " +
                "COUNT(DISTINCT r.id) AS total_ratings, " +
                "STRING_AGG(meg.genre::TEXT, ',') AS genres " +
                "FROM media_entry m " +
                "JOIN app_user u ON m.creator_id = u.user_id " +
                "LEFT JOIN media_entry_genre meg ON m.id = meg.media_entry_id " +
                "LEFT JOIN rating r ON m.id = r.media_entry_id " +
                "WHERE m.id NOT IN ( " +
                "    SELECT media_entry_id FROM rating WHERE user_id = ? " +
                ") " +
                "AND (" + conditions + ") " +
                "GROUP BY m.id, u.username " +
                "HAVING COALESCE(AVG(r.stars_ct), 0) >= 3.5 OR COUNT(DISTINCT r.id) = 0 " +
                "ORDER BY avg_rating DESC, total_ratings DESC " +
                "LIMIT 10", params.toArray());
    }


    private <T> void appendINClause(String columnName, List<T> values, StringBuilder filterConditions, List<Object> filterParams) {
        if (values == null || values.isEmpty()) return;

        if(!filterConditions.isEmpty()) filterConditions.append(" OR ");

        filterConditions.append(columnName).append(" IN (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) filterConditions.append(", ");
            filterConditions.append("?");
            filterParams.add(values.get(i));
        }
        filterConditions.append(")");
    }

}
