package com.applications.bobatea.controllers;

import com.applications.bobatea.exceptions.UserExistsException;
import com.applications.bobatea.models.User;
import com.applications.bobatea.repository.UserRepository;
import com.applications.bobatea.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RegistrationControllerIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:9.1")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    public static void setup() {
        mySQLContainer.start();
        System.setProperty("spring.datasource.url", mySQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", mySQLContainer.getUsername());
        System.setProperty("spring.datasource.password", mySQLContainer.getPassword());
    }

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @AfterAll
    public static void tearDown() {
        mySQLContainer.stop();
    }

    @Test
    public void RegistrationTest_Success() throws UserExistsException {

        User user = new User("user", "password");

        userService.register(user);

        Optional<User> user1 = userRepository.findByUsername("user");

        assertTrue(user1.isPresent());
        assertEquals("user", user1.get().getUsername());
        assertTrue(passwordEncoder.matches("password", user1.get().getPassword()));
    }

    @Test
    public void RegistrationTest_UserExists() throws UserExistsException {
        User user = new User("user", "password");
        userService.register(user);

        assertThrows(UserExistsException.class, () -> userService.register(user));
    }

}