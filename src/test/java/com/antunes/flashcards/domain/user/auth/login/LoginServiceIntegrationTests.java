package com.antunes.flashcards.domain.user.auth.login;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.model.LoginAttempt;
import com.antunes.flashcards.domain.user.auth.repository.LoginAttemptRepository;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.exception.UserTimeoutException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import com.antunes.flashcards.infrastructure.time.ClockService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Import(FixedClockTestConfig.class)
@ActiveProfiles("test")
public class LoginServiceIntegrationTests {
  @Autowired private LoginService loginService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
  @Autowired private ClockService clockService;
  @Autowired private LoginAttemptService loginAttemptService;
  @Autowired private LoginAttemptRepository loginAttemptRepository;
  @Autowired private FixedClockTestConfig fixedClockTestConfig;

  private final String rawEmail = "user@example.com";
  private final String notRegisteredEmail = "notthere@example.com";
  private final String rawPassword = "securePassword123";
  private final String incorrectPassword = "notThePassword";

  private User user;

  @BeforeAll
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userRepository.flush();
    user = userService.register(rawEmail, rawPassword);
    userRepository.flush();
  }

  @BeforeEach
  void cleanup() {
    Clock originalClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    clockService.setClock(originalClock);
    LoginAttempt loginAttempt = loginAttemptService.getOrCreate(user);
    loginAttempt.unlock();
    loginAttemptRepository.saveAndFlush(loginAttempt);
  }

  @Test
  void registeredUserCanLoginCorrectPassword() {
    String token = loginService.login(rawEmail, rawPassword);
    assertNotNull(token);
  }

  @Test
  void registeredUserCannotLoginIncorrectPassword_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class,
            () -> loginService.login(rawEmail, incorrectPassword));
    assertEquals("Incorrect password", exception.getMessage());
  }

  @Test
  void unregisteredUserCannotLogin_shouldThrow() {
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> loginService.login(notRegisteredEmail, rawPassword));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void loginShouldReturnValidJwtTokenContainingUserEmail() {
    String token = loginService.login(rawEmail, rawPassword);
    assertDoesNotThrow(
        () -> {
          Claims claims =
              Jwts.parser()
                  .verifyWith(jwtTokenProvider.getSecretKey())
                  .build()
                  .parseSignedClaims(token)
                  .getPayload();
          assertEquals(rawEmail, claims.getSubject());
          assertEquals(TokenType.AUTH.name(), claims.get("type"));
        });
  }

  @Test
  void onFiveFailedLoginAttempts_shouldLock() {
    for (int i = 0; i < 5; i++) {
      assertThrows(
          PasswordValidationException.class, () -> loginService.login(rawEmail, incorrectPassword));
    }
    UserTimeoutException exception =
        assertThrows(
            UserTimeoutException.class, () -> loginService.login(rawEmail, incorrectPassword));
    assertEquals("Your account was timedout for 15 minutes", exception.getMessage());
  }

  @Test
  void onSuccessfulLogin_shouldResetCount() {
    for (int i = 0; i < 4; i++) {
      assertThrows(
          PasswordValidationException.class, () -> loginService.login(rawEmail, incorrectPassword));
    }
    int attemptCount = loginAttemptRepository.findByUser(user).get().getAttemptCount();
    assertEquals(4, attemptCount);
    assertDoesNotThrow(() -> loginService.login(rawEmail, rawPassword));
    attemptCount = loginAttemptRepository.findByUser(user).get().getAttemptCount();
    assertEquals(0, attemptCount);
  }

  @Test
  void shouldUnlockAfter15Minutes() {
    for (int i = 0; i < 5; i++) {
      assertThrows(
          PasswordValidationException.class, () -> loginService.login(rawEmail, incorrectPassword));
    }
    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(16));
    assertDoesNotThrow(() -> loginService.login(rawEmail, rawPassword));
    int attemptCount = loginAttemptRepository.findByUser(user).get().getAttemptCount();
    assertEquals(0, attemptCount);
  }

  @Test
  void multipleSuccessfulLogins_shouldAlwaysSucceed() {
    for (int i = 0; i < 3; i++) {
      String token = loginService.login(rawEmail, rawPassword);
      assertNotNull(token);
      assertEquals(0, loginAttemptRepository.findByUser(user).get().getAttemptCount());
    }
  }
}
