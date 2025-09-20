package com.hamza.JobScout.service;

import com.hamza.JobScout.entity.RefreshToken;
import com.hamza.JobScout.entity.User;
import com.hamza.JobScout.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    // NEW: Maximum absolute session duration (e.g., 30 days)
    @Value("${jwt.session.max.duration:2592000}") // 30 days default
    private Long maxSessionDuration;

    /**
     * Create initial refresh token for login
     */
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, LocalDateTime.now());
    }

    /**
     * Create refresh token with specific session start time
     * This preserves the original session start time for absolute timeout
     */
    public RefreshToken createRefreshToken(User user, LocalDateTime sessionStartTime) {
        // Revoke all existing tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setSessionStartTime(sessionStartTime);
        refreshToken.setTokenIssuedAt(LocalDateTime.now());
        
        // Set token expiry to the shorter of: refresh token duration OR remaining session time
        LocalDateTime maxSessionEnd = sessionStartTime.plusSeconds(maxSessionDuration);
        LocalDateTime tokenExpiry = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);
        
        // Use the earlier of the two dates
        refreshToken.setExpiryDate(tokenExpiry.isBefore(maxSessionEnd) ? tokenExpiry : maxSessionEnd);
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Create new refresh token during token refresh, preserving original session start time
     */
    public RefreshToken refreshToken(RefreshToken oldToken) {
        if (oldToken.getSessionStartTime() == null) {
            // Handle legacy tokens - treat token creation time as session start
            return createRefreshToken(oldToken.getUser(), oldToken.getCreatedAt());
        }
        
        // Preserve original session start time
        return createRefreshToken(oldToken.getUser(), oldToken.getSessionStartTime());
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        // Check both token expiry and absolute session timeout
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        if (token.isSessionExpired(maxSessionDuration)) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Session has expired. Maximum session duration exceeded. Please login again.");
        }

        return token;
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    // Clean up expired and revoked tokens every hour
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredAndRevokedTokens(now);
        
        // Also cleanup tokens that have exceeded max session duration
        LocalDateTime maxSessionCutoff = now.minusSeconds(maxSessionDuration);
        refreshTokenRepository.deleteBySessionStartTimeBefore(maxSessionCutoff);
    }

    // Getter for max session duration (for testing or monitoring)
    public Long getMaxSessionDuration() {
        return maxSessionDuration;
    }
}
