package org.mrp.model;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

public class MediaEntry {
    private UUID id;
    protected String title;
    protected String description;
    protected MediaEntryType type; //VERWENDUNG: type = MediaEntryType.GAME;
    protected Year releaseYear;
    protected int ageRestriction;
    protected List<Genre> genres;
    private User creator;
    private LocalDateTime createdAt;
    //protected List<Rating> ratings;

    public MediaEntry(String title, String description, Year releaseYear, int ageRestriction, User creator) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.creator = creator;
    }

    public UUID getId() {
        return id;
    }

    public User getCreator() {
        return creator;
    }

    public double getAvgScore() {
        return 0; //CHANGE
    }
}
