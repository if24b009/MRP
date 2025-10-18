package org.mrp.dto;

import org.mrp.model.Genre;
import org.mrp.model.MediaEntryType;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public class MediaEntryTO {
    private UUID id;
    private String title;
    private String description;
    private MediaEntryType type;
    private int releaseYear;
    private int ageRestriction;
    private List<Genre> genres;
    private UUID creatorId;

    public MediaEntryTO(UUID id, String title, String description, MediaEntryType type, int releaseYear, int ageRestriction, List<Genre> genres,  UUID creatorId) {
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
}
