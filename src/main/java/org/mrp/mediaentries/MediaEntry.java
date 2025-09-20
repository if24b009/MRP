package org.mrp.mediaentries;

import java.time.Year;
import java.util.List;

public abstract class MediaEntry {
    private int id;
    protected String title;
    protected String description;
    protected char type;
    protected Year releaseYear;
    protected List<String> genres;
    protected int ageRestriction;

    public MediaEntry(String title, String description, Year releaseYear, int ageRestriction) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
    }

    public double getAvgScore() {
        return 0; //CHANGE
    }

    public abstract void onlyForTesting(); //REMOVE
}
