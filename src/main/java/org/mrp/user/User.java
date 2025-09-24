package org.mrp.user;

import org.mrp.mediaentries.MediaEntry;

import java.util.List;

public class User {
    private String username;

    private List<MediaEntry> favorites;
    //private Profile profile;
    //private List<Rating> ratingHistory;
    private List<MediaEntry> recommendations;

    public User(String username) {
        this.username = username;
    }

    /*public boolean isUserCreator(MediaEntry entry) {
        if (entry.getCreator() != this) return false;
        return true;
    }*/

    /*public void editMediaEntry(MediaEntry entry) {
        if(this.isUserCreator(entry)) {

        }
        else System.out.println("ERROR: This user is not the creator.");
    }

    public void deleteMediaEntry(MediaEntry entry) {
        if(this.isUserCreator(entry)) {

        }
        else System.out.println("ERROR: This user is not the creator.");
    }*/

    public String getUsername() {
        return username;
    }

    public List<MediaEntry> getFavorites() {
        return this.favorites;
    }

    /*public List<Rating> getRatingHistory() {
        return this.ratingHistory;
    }*/
}
