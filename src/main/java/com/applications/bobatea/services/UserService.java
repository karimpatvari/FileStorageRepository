package com.applications.bobatea.services;

import com.applications.bobatea.exceptions.UserExistsException;
import com.applications.bobatea.models.User;

import java.util.Optional;

public interface UserService {

    User register(User user) throws UserExistsException;

    Optional<User> findByUsername(String username);
}
