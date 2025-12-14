package org.mrp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Rating {
    private UUID id;
    private UUID userId;
    private UUID mediaEntryId;
    private String comment;
    int stars_ct;
    private boolean isCommentVisible;
    private LocalDateTime timestamp;
    private List<User> likedBy = new ArrayList<>();

    public Rating() {
    }

    public Rating(UUID userId, UUID mediaEntryId, int stars_ct, String comment, LocalDateTime timestamp) {
        this.userId = userId;
        this.mediaEntryId = mediaEntryId;
        this.stars_ct = stars_ct;
        this.comment = comment;
        this.isCommentVisible = false;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMediaEntryId() {
        return mediaEntryId;
    }

    public String getComment() {
        return comment;
    }

    public int getStars_ct() {
        return stars_ct;
    }

    public void setStars_ct(int stars_ct) {
        this.stars_ct = stars_ct;
    }

    public boolean isCommentVisible() {
        return isCommentVisible;
    }

    public void setCommentVisible() {
        isCommentVisible = true;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<User> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<User> likedBy) {
        this.likedBy = likedBy;
    }
}
