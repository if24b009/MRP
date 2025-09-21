package org.mrp;

import org.mrp.mediaentries.Game;
import org.mrp.mediaentries.MediaEntry;
import org.mrp.mediaentries.Movie;
import org.mrp.mediaentries.Series;
import org.mrp.user.User;

import java.time.Year;

public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.createMediaEntry('g', "Movie", "test", Year.of(2018), 18);

        MediaEntry movie = new Movie("Movie", "test", Year.of(2018), 18, user);
        MediaEntry series = new Series("Series", "test", Year.of(2018), 18, user);
        MediaEntry game = new Game("Game", "test", Year.of(2018), 18, user);

        movie.onlyForTesting();
        series.onlyForTesting();
        game.onlyForTesting();
    }
}