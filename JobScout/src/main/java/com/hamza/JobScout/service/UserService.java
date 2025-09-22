package com.hamza.JobScout.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hamza.JobScout.entity.User;
import com.hamza.JobScout.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    public boolean userExists(String email) {
        return findByEmail(email).isPresent();
    }

    public User createUser(String fullName, String email, String encodedPassword, String serpapiKey) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setSerpapiKey(serpapiKey);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // New method to get API key for current user
    public String getApiKeyForUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return userOptional.get().getSerpapiKey();
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    // Alternative method to get API key by email
    public String getApiKeyForUser(String email) {
        Optional<User> userOptional = findByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get().getSerpapiKey();
        }
        throw new RuntimeException("User not found with email: " + email);
    }
}
