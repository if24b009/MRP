package org.mrp.mediaentries;

import org.mrp.user.User;

import java.time.Year;

public class Movie extends MediaEntry {
    private char type;

    public Movie(String title, String description, Year releaseYear, int ageRestriction, User creator) {
        super(title, description, releaseYear, ageRestriction, creator);
        this.type = 'm';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a movie with the title: " + title);
    }

}
