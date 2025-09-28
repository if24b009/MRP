package org.mrp.repository;

import org.mrp.model.User;

import java.util.List;

public interface UserRepository {
    public User save(User user);
    public List<User> findById(int id);
    public List<User> findByUsername(String username);
    public List<User> findAll();
    public void delete(int id);
}
