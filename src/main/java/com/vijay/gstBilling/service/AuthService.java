package com.vijay.gstBilling.service;

import com.vijay.gstBilling.config.JwtConfig;
import com.vijay.gstBilling.config.RabbitMQConfig;
import com.vijay.gstBilling.dto.auth.LoginRequest;
import com.vijay.gstBilling.dto.auth.RegisterRequest;
import com.vijay.gstBilling.dto.email.EmailVerificationMessage;
import com.vijay.gstBilling.entity.EmailVerification;
import com.vijay.gstBilling.entity.RefreshToken;
import com.vijay.gstBilling.entity.User;
import com.vijay.gstBilling.exception.BadRequestException;
import com.vijay.gstBilling.exception.UnauthorizedException;
import com.vijay.gstBilling.repository.EmailVerificationRepository;
import com.vijay.gstBilling.repository.RefreshTokenRepository;
import com.vijay.gstBilling.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final long EMAIL_TOKEN_EXPIRY_MS = 86_400_000L;
    private static final int MAX_REFRESH_TOKENS = 2;

    public AuthService(UserRepository userRepository,
                       EmailVerificationRepository emailVerificationRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JwtConfig jwtConfig,
                       RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .build();
        userRepository.save(user);
        log.info("Registered user: {}", user.getEmail());

        String rawToken = UUID.randomUUID().toString();
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .tokenHash(sha256(rawToken))
                .expiresAt(Instant.now().plusMillis(EMAIL_TOKEN_EXPIRY_MS))
                .build();
        emailVerificationRepository.save(verification);

        String verifyLink = baseUrl + "/api/auth/verify-email?token=" + rawToken;

        // NEW (async via RabbitMQ):
        EmailVerificationMessage message = new EmailVerificationMessage(user.getEmail(),user.getName(),verifyLink);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );

        log.info("Published email verification message for: {}", user.getEmail());
    }

    @Transactional
    public String[] login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified. Check your inbox.");
        }

        // Revoke oldest token if at limit
        List<RefreshToken> active = refreshTokenRepository
                .findByUserAndRevokedFalseOrderByCreatedAtAsc(user);
        if (active.size() >= MAX_REFRESH_TOKENS) {
            active.get(0).setRevoked(true);
            refreshTokenRepository.save(active.get(0));
            log.info("Revoked oldest refresh token for: {}", user.getEmail());
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String rawRefresh = UUID.randomUUID().toString();
        saveRefreshToken(user, rawRefresh);

        log.info("User logged in: {}", user.getEmail());
        return new String[]{accessToken, rawRefresh};
    }

    @Transactional
    public String[] refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token missing");
        }

        String hash = sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String newAccess = jwtService.generateAccessToken(user.getEmail());
        String newRawRefresh = UUID.randomUUID().toString();
        saveRefreshToken(user, newRawRefresh);

        log.info("Tokens rotated for: {}", user.getEmail());
        return new String[]{newAccess, newRawRefresh};
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Logged out user: {}", token.getUser().getEmail());
        });
    }

    private void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(rawToken))
                .expiresAt(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiry()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}