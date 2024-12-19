package com.applications.bobatea.services.impl;

import com.applications.bobatea.customExceptions.UserExistsException;
import com.applications.bobatea.models.User;
import com.applications.bobatea.repository.UserRepository;
import com.applications.bobatea.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) throws UserExistsException {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserExistsException(user.getUsername() + " already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getAuthenticatedUser(Principal principal) throws UsernameNotFoundException {
        return findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal.getName()));
    }


}
