package com.antunes.flashcards.domain.user.auth.reset;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.TokenValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class PasswordResetIntegrationTests {
  private final String rawEmail = "user@email.com";
  private final String randomEmail = "random@example.com";
  private final String rawPassword = "securePassword123";
  private final String newRawPassword = "newPassword123";

  @Autowired private PasswordResetService passwordResetService;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private User user;

  @BeforeAll
  void setUp() {
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userService.register(rawEmail, rawPassword);
  }

  @BeforeEach
  void fetchUser() {
    user = userService.findByEmail(rawEmail).get();
  }

  public String generateExpiredResetToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .claim("type", TokenType.RESET)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(jwtTokenProvider.getSecretKey())
        .compact();
  }

  @Test
  void registeredUserWithValidResetToken_shouldNotThrow() {
    String token = passwordResetService.reset(rawEmail);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token, TokenType.RESET));
  }

  @Test
  void unregisteredUser_shouldThrow() {
    String differentEmail = "other@example.com";
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> passwordResetService.reset(differentEmail));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void registeredUser_shouldReturnValidToken() {
    String token = passwordResetService.reset(rawEmail);
    assertDoesNotThrow(
        () -> {
          Claims claims =
              Jwts.parser()
                  .verifyWith(jwtTokenProvider.getSecretKey())
                  .build()
                  .parseSignedClaims(token)
                  .getPayload();
          assertEquals(rawEmail, claims.getSubject());
          assertEquals(TokenType.RESET.name(), claims.get("type"));
        });
  }

  @Test
  void registeredUserWithValidToken_shouldChangePassword() {
    String token = passwordResetService.reset(rawEmail);
    assertDoesNotThrow(() -> passwordResetService.resetPassword(token, newRawPassword));
    User savedUser = userService.findByEmail(rawEmail).get();
    assertFalse(passwordEncoder.matches(rawPassword, savedUser.getHashedPassword()));
    assertTrue(passwordEncoder.matches(newRawPassword, savedUser.getHashedPassword()));
    assertNotNull(savedUser.getId());
    assertEquals(savedUser.getId(), user.getId());
  }

  @Test
  void invalidToken_shouldThrow() {
    String badToken = "invalid.token.value";
    TokenValidationException exception =
        assertThrows(
            TokenValidationException.class,
            () -> {
              passwordResetService.resetPassword(badToken, newRawPassword);
            });
    assertEquals("Invalid token", exception.getMessage());
  }

  @Test
  void validTokenButUserNotFound_shouldThrow() {
    String token = jwtTokenProvider.generateResetToken(randomEmail, 1L);
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("User not found for the provided reset token", exception.getMessage());
  }

  @Test
  void invalidNewPassword_shouldThrow() {
    String token = passwordResetService.reset(rawEmail);
    String invalidNewPassword = "123";
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class,
            () -> {
              passwordResetService.resetPassword(token, invalidNewPassword);
            });
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }

  @Test
  void authToken_shouldThrow() {
    String token = jwtTokenProvider.generateAuthToken(rawEmail, 1L);
    TokenValidationException exception =
        assertThrows(
            TokenValidationException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("Unexpected token type", exception.getMessage());
  }

  @Test
  void expiredToken_shouldThrow() {
    String token = generateExpiredResetToken(rawEmail);
    TokenExpiredException exception =
        assertThrows(
            TokenExpiredException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("Token has expired", exception.getMessage());
  }
}
