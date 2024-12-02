package com.applications.bobatea.services;

import com.applications.bobatea.exceptions.UserExistsException;
import com.applications.bobatea.models.User;

public interface UserService {

    void register(User user) throws UserExistsException;

}
