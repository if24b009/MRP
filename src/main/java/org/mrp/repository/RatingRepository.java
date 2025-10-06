package org.mrp.repository;

import org.mrp.model.Rating;

import java.util.List;
import java.util.UUID;

public class RatingRepository {
    public Rating save(Rating rating) {
        return null;
    }

    public Rating findById(UUID id) {
        return null;
    }

    public List<Rating> findByMediaEntryId(UUID id) {
        return null;
    }

    public List<Rating> findByUser(UUID id) {
        return null;
    }

    public List<Rating> findByUserAndMediaEntry(UUID userId, UUID mediaEntryId) {
        return null;
    }

    public void delete(UUID id) {

    }
}
