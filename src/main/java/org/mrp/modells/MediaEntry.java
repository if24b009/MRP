package org.mrp.modells;

import java.time.Year;
import java.util.List;

public class MediaEntry {
    private int id;
    protected String title;
    protected String description;
    protected char type;
    protected Year releaseYear;
    protected List<String> genres;
    protected int ageRestriction;
    private User creator;
    //protected List<Rating> ratings;

    public MediaEntry(String title, String description, Year releaseYear, int ageRestriction, User creator) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        this.creator = creator;
    }

    public User getCreator() {
        return creator;
    }

    public double getAvgScore() {
        return 0; //CHANGE
    }
}
