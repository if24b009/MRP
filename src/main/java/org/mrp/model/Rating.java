package org.mrp.model;

import java.time.LocalDateTime;
import java.util.List;

public class Rating {
    private int id;
    private int userId;
    private int mediaEntryId;
    private int starsCt;
    private String comment;
    private boolean isCommentVisible;
    private LocalDateTime timestamp;
    private List<User> likedBy;

    public Rating(int userId, int mediaEntryId, String comment, LocalDateTime timestamp) {
        this.userId = userId;
        this.mediaEntryId = mediaEntryId;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getMediaEntryId() {
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
