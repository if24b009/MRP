package org.mrp.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MediaEntry {
    private UUID id;
    private String title;
    private String description;
    private MediaEntryType type; //VERWENDUNG: type = MediaEntryType.GAME;
    private int releaseYear;
    private int ageRestriction;
    private List<Genre> genres;
    private UUID creatorId;
    private LocalDateTime createdAt;
    private double avgRating;

    public MediaEntry() {}

    public MediaEntry(String title, String description, int releaseYear, int ageRestriction, UUID creatorId) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.creatorId = creatorId;
    }

    public MediaEntry(UUID id, String title, String description, MediaEntryType type, int releaseYear, int ageRestriction, List<Genre> genres,  UUID creatorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.genres = genres;
        this.creatorId = creatorId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MediaEntryType getType() {
        return type;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setCreator(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(MediaEntryType type) {
        this.type = type;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}
