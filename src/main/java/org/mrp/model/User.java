package org.mrp.model;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String password_hashed;

    public User(String userId, String username, String password_hashed) {
        this.userId = userId;
        this.username = username;
        this.password_hashed = password_hashed;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password_hashed;
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
}
