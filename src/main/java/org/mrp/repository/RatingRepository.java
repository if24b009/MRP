package org.mrp.repository;

import org.mrp.model.Rating;
import org.mrp.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class RatingRepository implements Repository<Rating> {
    public RatingRepository() {
    }

    private UUID insertRating(Rating object) throws SQLException {
        return db.insert(
                "INSERT INTO rating (id, user_id, media_entry_id, stars_ct, comment, is_comment_visible, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                object.getUserId(),
                object.getMediaEntryId(),
                object.getStars_ct(),
                object.getComment(),
                object.isCommentVisible(),
                new Timestamp(System.currentTimeMillis())
        );
    }

    @Override
    public UUID save(Rating object) throws SQLException {
        UUID ratingId = insertRating(object);

        //Insert User likes in linking table (Zwischentabelle)
        for (User user : object.getLikedBy()) {
            db.insertWithoutUUID(
                    "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?)",
                    ratingId,
                    user.getUserId()
            );
        }

        return ratingId;
    }

    //Helperfunction to update likes for specific rating (in linking table)
    private void updateRatingLikes(UUID ratingId, List<User> likedBy) throws SQLException {
        //Remove all existing likes
        db.update("DELETE FROM rating_likes WHERE rating_id = ?", ratingId);

        //Insert new likes
        for (User user : likedBy) {
            db.insertWithoutUUID(
                    "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?)",
                    ratingId,
                    user.getUserId()
            );
        }
    }

    public int update(Rating object) throws SQLException {
        int rowsAffected = db.update(
                "UPDATE rating SET stars_ct = ?, comment = ?, is_comment_visible = ? " +
                        "WHERE id = ?",
                object.getStars_ct(),
                object.getComment(),
                object.isCommentVisible(),
                object.getId()
        );

        //Update linking table (Zwischentabelle) Rating-Links
        updateRatingLikes(object.getId(), object.getLikedBy());

        return rowsAffected;
    }


    @Override
    public ResultSet findById(UUID id) {
        return null;
    }

    @Override
    public int delete(UUID id) throws SQLException {
        return db.update("DELETE FROM rating WHERE id = ?", id);
    }

    @Override
    public ResultSet findAll() {
        return null;
    }

    public Object getCreatorObject(UUID ratingId) throws SQLException {
        return db.getValue("SELECT user_id FROM rating WHERE id = ?", ratingId);
    }

    public Object getRatingObject(UUID ratingId) throws SQLException {
        return db.getValue("SELECT * FROM rating WHERE id = ?", ratingId);
    }

    public int changeCommentVisibility(UUID ratingId) throws SQLException {
        return db.update(
                "UPDATE rating SET is_comment_visible = true WHERE id = ?",
                ratingId
        );
    }

    public boolean isAlreadyLikedByUser(UUID userId, UUID ratingId) throws SQLException {
        Object result = db.getValue(
                "SELECT 1 FROM rating_likes WHERE user_id = ? AND rating_id = ?",
                userId,
                ratingId
        );
        return result != null; //result == null -> true -> not liked yet
    }

    public void addUserLike(UUID userId, UUID ratingId) throws SQLException {
        db.insertWithoutUUID(
                "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?)",
                ratingId,
                userId
        );
    }

    public void removeUserLike(UUID userId, UUID ratingId) throws SQLException {
        db.update(
                "DELETE FROM rating_likes WHERE user_id = ? AND rating_id = ?",
                userId,
                ratingId
        );
    }

    //All Ratings of a user
    public ResultSet findByUserId(UUID userId) throws SQLException {
        return db.query(
                "SELECT r.*, COUNT(rl.user_id) AS like_count " +
                        "FROM rating r " +
                        "LEFT JOIN rating_likes rl ON r.id = rl.rating_id " +
                        "WHERE r.user_id = ? " +
                        "GROUP BY r.id " +
                        "ORDER BY r.timestamp DESC",
                userId
        );
    }

    //All ratings of a media entry
    public ResultSet findByMediaEntryId(UUID mediaEntryId) throws SQLException {
        return db.query(
                "SELECT r.*, COUNT(rl.user_id) AS like_count " +
                        "FROM rating r " +
                        "LEFT JOIN rating_likes rl ON r.id = rl.rating_id " +
                        "WHERE r.media_entry_id = ? " +
                        "GROUP BY r.id " +
                        "ORDER BY r.timestamp DESC",
                mediaEntryId
        );
    }



}
