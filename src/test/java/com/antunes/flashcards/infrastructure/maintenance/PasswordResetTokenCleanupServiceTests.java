package com.antunes.flashcards.infrastructure.maintenance;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.reset.PasswordResetService;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class PasswordResetTokenCleanupServiceTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";
  private final String newRawPassword = "newPassword123";

  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
  @Autowired private PasswordResetService passwordResetService;
  @Autowired private PasswordResetTokenCleanupService passwordResetTokenCleanupService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  private User user;

  @BeforeAll
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userService.register(rawEmail, rawPassword);
  }

  @BeforeEach
  void fetchUser() {
    user = userService.findByEmail(rawEmail).get();
  }

  @AfterEach
  void cleanup() {
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
  void cleanupShouldDeleteExpiredTokens() {
    String token = generateExpiredResetToken(rawEmail);
    Instant expiredAt = Instant.now().minus(Duration.ofMinutes(5));
    PasswordResetToken resetToken = new PasswordResetToken(user, token, expiredAt);
    passwordResetTokenRepository.save(resetToken);
    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(0, remaining.size());
  }

  @Test
  void cleanupShouldDeleteUsedTokens() {
    passwordResetService.resetPassword(passwordResetService.reset(rawEmail), newRawPassword);
    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(0, remaining.size());
  }

  @Test
  void cleanupShouldNotDeleteValidTokens() {
    passwordResetService.reset(rawEmail);
    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(1, remaining.size());
    assertEquals(jwtTokenProvider.parseToken(remaining.get(0).getToken()).getSubject(), rawEmail);
  }

  @Test
  void cleanupShouldDeleteAllButOne() {
    String token = generateExpiredResetToken(rawEmail);
    Instant expiredAt = Instant.now().minus(Duration.ofMinutes(5));
    PasswordResetToken resetToken = new PasswordResetToken(user, token, expiredAt);

    passwordResetService.resetPassword(passwordResetService.reset(rawEmail), newRawPassword);
    passwordResetTokenRepository.save(resetToken);

    passwordResetService.reset(rawEmail);

    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(1, remaining.size());
    assertEquals(jwtTokenProvider.parseToken(remaining.get(0).getToken()).getSubject(), rawEmail);
  }

  @Test
  void cleanupShouldPreserveMultipleValidTokens() {
    passwordResetService.reset(rawEmail);
    passwordResetService.reset(rawEmail);
    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(2, remaining.size());
  }

  @Test
  void tokenExpiringExactlyNowShouldBeConsideredExpired() {
    Instant now = Instant.now();
    String token = jwtTokenProvider.generateResetToken(rawEmail, user.getId());
    PasswordResetToken resetToken = new PasswordResetToken(user, token, now);
    passwordResetTokenRepository.save(resetToken);

    passwordResetTokenCleanupService.cleanupExpiredOrUsedTokens();
    List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
    assertEquals(0, remaining.size());
  }
}
