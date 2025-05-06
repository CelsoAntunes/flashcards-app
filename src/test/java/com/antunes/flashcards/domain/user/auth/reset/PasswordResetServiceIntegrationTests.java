package com.antunes.flashcards.domain.user.auth.reset;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flashcard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.*;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
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
public class PasswordResetServiceIntegrationTests {
  private final String rawEmail = "user@email.com";
  private final String randomEmail = "random@example.com";
  private final String rawPassword = "securePassword123";
  private final String newRawPassword = "newPassword123";

  @Autowired private PasswordResetService passwordResetService;
  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  private User user;

  @PersistenceContext EntityManager entityManager;

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

  @BeforeEach
  void clean() {
    passwordResetTokenRepository.deleteAll();
  }

  public String generateExpiredResetToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .claim("type", TokenType.RESET)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(Duration.ofMinutes(5))))
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

  @Transactional
  @Test
  void registeredUserWithValidToken_shouldChangePassword() {
    String token = passwordResetService.reset(rawEmail);
    assertDoesNotThrow(() -> passwordResetService.resetPassword(token, newRawPassword));
    entityManager.flush();
    entityManager.clear();
    User savedUser = userService.findByEmail(rawEmail).get();
    assertFalse(passwordEncoder.matches(rawPassword, savedUser.getHashedPassword()));
    assertTrue(passwordEncoder.matches(newRawPassword, savedUser.getHashedPassword()));
    assertNotNull(savedUser.getId());
    assertEquals(savedUser.getId(), user.getId());
  }

  @Test
  void invalidToken_shouldThrow() {
    String badToken = "invalid.token.value";
    ResetTokenNotFoundException exception =
        assertThrows(
            ResetTokenNotFoundException.class,
            () -> {
              passwordResetService.resetPassword(badToken, newRawPassword);
            });
    assertEquals("Reset token not found", exception.getMessage());
  }

  @Test
  void validTokenButNoUser_shouldThrow() {
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> passwordResetService.reset(randomEmail));
    assertEquals("No accounts with this email", exception.getMessage());
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
    ResetTokenNotFoundException exception =
        assertThrows(
            ResetTokenNotFoundException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("Reset token not found", exception.getMessage());
  }

  @Test
  void expiredToken_shouldThrow() {
    String token = generateExpiredResetToken(rawEmail);
    Instant expiredAt = Instant.now().minus(Duration.ofMinutes(5));
    PasswordResetToken resetToken = new PasswordResetToken(user, token, expiredAt);
    passwordResetTokenRepository.save(resetToken);
    TokenExpiredException exception =
        assertThrows(
            TokenExpiredException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("Token is either expired or already used", exception.getMessage());
  }

  @Test
  void usedToken_shouldThrow() {
    String token = passwordResetService.reset(rawEmail);
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).get();
    resetToken.markAsUsed();
    passwordResetTokenRepository.save(resetToken);
    TokenExpiredException exception =
        assertThrows(
            TokenExpiredException.class,
            () -> {
              passwordResetService.resetPassword(token, newRawPassword);
            });
    assertEquals("Token is either expired or already used", exception.getMessage());
  }

  @Test
  void validTokenShouldBeMarkedAsUsed() {
    String token = passwordResetService.reset(rawEmail);
    passwordResetService.resetPassword(token, newRawPassword);
    PasswordResetToken tokenEntity = passwordResetTokenRepository.findByToken(token).get();
    assertTrue(tokenEntity.isUsed());
  }
}
