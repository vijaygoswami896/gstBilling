package com.vijay.gstBilling.controller;

import com.vijay.gstBilling.dto.auth.AuthResponse;
import com.vijay.gstBilling.dto.auth.LoginRequest;
import com.vijay.gstBilling.dto.auth.RegisterRequest;
import com.vijay.gstBilling.entity.EmailVerification;
import com.vijay.gstBilling.exception.BadRequestException;
import com.vijay.gstBilling.repository.EmailVerificationRepository;
import com.vijay.gstBilling.repository.UserRepository;
import com.vijay.gstBilling.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-token-expiry}")
    private int refreshTokenExpiry;

    private static final String REFRESH_COOKIE = "refreshToken";

    public AuthController(AuthService authService,
                          EmailVerificationRepository emailVerificationRepository,
                          UserRepository userRepository) {
        this.authService = authService;
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful. Check your email to verify your account."));
    }

    @GetMapping("/verify-email")
    @Transactional
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        String tokenHash = AuthService.sha256(token);

        EmailVerification verification = emailVerificationRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (verification.getUsedAt() != null) {
            throw new BadRequestException("Token already used");
        }
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        verification.setUsedAt(Instant.now());
        emailVerificationRepository.save(verification);
        verification.getUser().setEmailVerified(true);
        userRepository.save(verification.getUser());

        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        String[] tokens = authService.login(request);
        setRefreshCookie(response, tokens[1]);
        return ResponseEntity.ok(new AuthResponse(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request,
                                                HttpServletResponse response) {
        String rawRefresh = readRefreshCookie(request);
        String[] tokens = authService.refresh(rawRefresh);
        setRefreshCookie(response, tokens[1]);
        return ResponseEntity.ok(new AuthResponse(tokens[0]));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response) {
        String rawRefresh = readRefreshCookie(request);
        authService.logout(rawRefresh);
        clearRefreshCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private void setRefreshCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set true in production (HTTPS)
        cookie.setPath("/api/auth");
        cookie.setMaxAge(refreshTokenExpiry / 1000);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}