package org.mrp.repository;

import org.mrp.model.MediaEntry;

import java.util.List;

public interface MediaEntryRepository {
    public MediaEntry save(MediaEntry mediaEntry);
    public MediaEntry findById(int id);
    public List<MediaEntry> findAll();
    public void delete(int id);
    public List<MediaEntry> findByCreator(int id);
}
