package org.mrp.user;

import org.mrp.mediaentries.Game;
import org.mrp.mediaentries.MediaEntry;
import org.mrp.mediaentries.Movie;
import org.mrp.mediaentries.Series;

import java.time.Year;
import java.util.List;

public class User {
    private List<MediaEntry> favorites;
    //private Profile profile;
    //private List<Rating> ratingHistory;
    private List<MediaEntry> recommendations;

    public User() {
    }

    public boolean isUserCreator(MediaEntry entry) {
        if(entry.getCreator() != this) return false;
        return true;
    }

    public void createMediaEntry(char type, String title, String description, Year releaseYear, int ageRestriction) {
        MediaEntry entry;
        if (type == 'm') {
            entry = new Movie(title, description, releaseYear, ageRestriction, this);
        } else if (type == 's') {
            entry = new Series(title, description, releaseYear, ageRestriction, this);
        } else if (type == 'g') {
            entry = new Game(title, description, releaseYear, ageRestriction, this);
        }
    }

    public void editMediaEntry(MediaEntry entry) {
        if(this.isUserCreator(entry)) {

        }
        else System.out.println("ERROR: This user is not the creator.");
    }

    public void deleteMediaEntry(MediaEntry entry) {
        if(this.isUserCreator(entry)) {

        }
        else System.out.println("ERROR: This user is not the creator.");
    }

    public void markMediaEntryAsFavorite(MediaEntry entry) {

    }

    public List<MediaEntry> getFavorites() {
        return this.favorites;
    }

    /*public List<Rating> getRatingHistory() {
        return this.ratingHistory;
    }*/
}
