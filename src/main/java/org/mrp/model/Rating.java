package org.mrp.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Rating {
    private UUID id;
    private UUID userId;
    private UUID mediaEntryId;
    private int starsCt;
    private String comment;
    private boolean isCommentVisible;
    private LocalDateTime timestamp;
    private List<User> likedBy;

    public Rating(UUID userId, UUID mediaEntryId, String comment, LocalDateTime timestamp) {
        this.userId = userId;
        this.mediaEntryId = mediaEntryId;
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

    public int getStarsCt() {
        return starsCt;
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
}
