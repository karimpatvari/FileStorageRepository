package com.applications.bobatea.services;

import com.applications.bobatea.customExceptions.UserExistsException;
import com.applications.bobatea.models.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.util.Optional;

public interface UserService {

    User register(User user) throws UserExistsException;

    Optional<User> findByUsername(String username);

    User getAuthenticatedUser(Principal principal) throws UsernameNotFoundException;


}
