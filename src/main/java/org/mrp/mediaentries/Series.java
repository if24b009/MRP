package org.mrp.mediaentries;

import java.time.Year;

public class Series extends MediaEntry{
    private char type;

    public Series(String title, String description, Year releaseYear, int ageRestriction) {
        super(title, description, releaseYear, ageRestriction);
        this.type = 's';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a series with the title: " + title);
    }
}
