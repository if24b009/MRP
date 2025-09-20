package org.mrp.mediaentries;

import java.time.Year;

public class Movie extends MediaEntry {
    private char type;

    public Movie(String title, String description, Year releaseYear, int ageRestriction) {
        super(title, description, releaseYear, ageRestriction);
        this.type = 'm';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a movie with the title: " + title);
    }

}
