package org.mrp.model;

import java.time.LocalDateTime;
import java.time.Year;
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
    private UUID creator;
    private LocalDateTime createdAt;
    //protected List<Rating> ratings;

    public MediaEntry() {}

    public MediaEntry(String title, String description, int releaseYear, int ageRestriction, UUID creator) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.creator = creator;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreator() {
        return creator;
    }

    public double getAvgScore() {
        return 0; //CHANGE
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

    public void setCreator(UUID creatorId) {
        this.creator = creator;
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
}
