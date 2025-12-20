package org.mrp.model;

import java.util.UUID;

public class User {
    private UUID userId;
    private String username;
    private String password_hashed;

    //For User Statistics (Profile)
    private int ratings_total;
    private int favorites_total;
    private int mediaEntriesCreated_total;
    private double avgScore;

    public User(String username, String password_hashed) {
        this.username = username;
        this.password_hashed = password_hashed;
    }

    public User(UUID userId, String username, String password_hashed) {
        this.userId = userId;
        this.username = username;
        this.password_hashed = password_hashed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPassword_hashed() {
        return password_hashed;
    }



    //For User Statistics (Profile)
    public void setRatings_total(int ratings_total) {
        this.ratings_total = ratings_total;
    }

    public void setFavorites_total(int favorites_total) {
        this.favorites_total = favorites_total;
    }

    public void setMediaEntriesCreated_total(int mediaEntriesCreated_total) {
        this.mediaEntriesCreated_total = mediaEntriesCreated_total;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }
}
