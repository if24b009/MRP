package org.mrp.mediaentries;

import java.time.Year;

public class Game extends MediaEntry{
    private char type;

    public Game(String title, String description, Year releaseYear, int ageRestriction) {
        super(title, description, releaseYear, ageRestriction);
        this.type = 'g';
    }

    @Override
    public void onlyForTesting() {
        System.out.println("I'm a game with the title: " + title);
    }
}
