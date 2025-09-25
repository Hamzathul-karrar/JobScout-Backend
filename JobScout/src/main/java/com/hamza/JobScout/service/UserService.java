package com.hamza.JobScout.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setApiCallCount(0);
        user.setLastApiResetDate(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Existing method to get API key for current user
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

    // New API tracking methods
    @Transactional
    public void incrementApiCallCount(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if we need to reset the monthly counter
            resetApiCountIfNeeded(user);
            
            // Increment the counter
            user.setApiCallCount(user.getApiCallCount() + 1);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Transactional
    public Integer getCurrentApiCallCount(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if we need to reset the monthly counter before returning
            resetApiCountIfNeeded(user);
            
            return user.getApiCallCount();
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Transactional
    private void resetApiCountIfNeeded(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastReset = user.getLastApiResetDate();
        
        // Check if 30 days have passed since last reset
        if (lastReset != null && lastReset.plusDays(30).isBefore(now)) {
            user.setApiCallCount(0);
            user.setLastApiResetDate(now);
            userRepository.save(user);
        }
    }

    @Transactional
    public void resetApiCallCount(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setApiCallCount(0);
            user.setLastApiResetDate(LocalDateTime.now());
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
}
