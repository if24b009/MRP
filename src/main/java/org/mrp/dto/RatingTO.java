package org.mrp.dto;

import org.mrp.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RatingTO {
    private UUID id;
    private UUID userId;
    private UUID mediaEntryId;
    private int stars_ct;
    private String comment;
    private boolean isCommentVisible;
    private LocalDateTime timestamp;
    private List<User> likedBy = new ArrayList<>();

    public RatingTO() {
    }

    public RatingTO(UUID userId, UUID mediaEntryId, int stars_ct, String comment, LocalDateTime timestamp) {
        this.userId = userId;
        this.mediaEntryId = mediaEntryId;
        this.stars_ct = stars_ct;
        this.isCommentVisible = false;
        this.comment = comment;
        this.timestamp = timestamp;
    }
    public RatingTO(UUID id, UUID userId, UUID mediaEntryId, int stars_ct, String comment, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.mediaEntryId = mediaEntryId;
        this.stars_ct = stars_ct;
        this.isCommentVisible = false;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMediaEntryId() {
        return mediaEntryId;
    }

    public int getStars_ct() {
        return stars_ct;
    }

    public String getComment() {
        return comment;
    }

    public boolean isCommentVisible() {
        return isCommentVisible;
    }

    public void setCommentVisible() {
        isCommentVisible = true;
    }

    public int getLikeCount() {
        return likedBy.toArray().length;
    }

    public List<User> getLikedBy() {
        return likedBy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
