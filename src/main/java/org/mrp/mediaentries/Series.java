package org.mrp.mediaentries;

import org.mrp.user.User;

import java.time.Year;

public class Series extends MediaEntry{
    private char type;

    public Series(String title, String description, Year releaseYear, int ageRestriction, User creator) {
        super(title, description, releaseYear, ageRestriction, creator);
        this.type = 's';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a series with the title: " + title);
    }
}
