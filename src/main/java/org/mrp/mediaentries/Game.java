package org.mrp.mediaentries;

import org.mrp.user.User;

import java.time.Year;

public class Game extends MediaEntry{
    private char type;

    public Game(String title, String description, Year releaseYear, int ageRestriction, User creator) {
        super(title, description, releaseYear, ageRestriction, creator);
        this.type = 'g';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a game with the title: " + title);
    }
}
