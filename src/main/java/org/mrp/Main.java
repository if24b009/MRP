package org.mrp;

import org.mrp.mediaentries.MediaEntry;
import org.mrp.user.User;

import java.time.Year;

public class Main {
    public static void main(String[] args) {
        User user = new User("UserA");
        MediaEntry mediaEntry = new MediaEntry("Test", "description", Year.of(2018), 16, user);
        System.out.println(mediaEntry.getCreator().getUsername());
    }
}