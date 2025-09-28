package org.mrp.repository;

import org.mrp.model.Rating;

import java.util.List;

public interface RatingRepository {
    public Rating save(Rating rating);
    public Rating findById(int id);
    public List<Rating> findByMediaEntryId(int id);
    public List<Rating> findByUser(int id);
    public List<Rating> findByUserAndMediaEntry(int userId, int mediaEntryId);
    public void delete(int id);
}
