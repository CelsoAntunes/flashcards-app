package com.antunes.flashcards.domain.user.auth.login;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.model.LoginAttempt;
import com.antunes.flashcards.domain.user.auth.repository.LoginAttemptRepository;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import com.antunes.flashcards.infrastructure.time.ClockService;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
@Transactional
public class LoginAttemptServiceTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  @Autowired private UserService userService;
  @Autowired private LoginAttemptService loginAttemptService;
  @Autowired private LoginService loginService;
  @Autowired private UserRepository userRepository;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
  @Autowired private LoginAttemptRepository loginAttemptRepository;
  @Autowired private FixedClockTestConfig fixedClockTestConfig;
  @Autowired private ClockService clockService;

  private User user;

  @BeforeAll
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    user = userService.register(rawEmail, rawPassword);
  }

  @BeforeEach
  void cleanup() {
    Clock originalClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    clockService.setClock(originalClock);
    LoginAttempt loginAttempt = loginAttemptService.getOrCreate(user);
    loginAttempt.resetAttempts();
    loginAttemptRepository.save(loginAttempt);
  }

  @Test
  void shouldIncrementFailedLoginAttempts() {
    loginAttemptService.registerFailedAttempt(user);
    assertEquals(1, loginAttemptService.getAttemptCount(user));
    loginAttemptService.registerFailedAttempt(user);
    assertEquals(2, loginAttemptService.getAttemptCount(user));
  }

  @Test
  void shouldResetCounterOnSuccessfulAttempt() {
    loginAttemptService.registerFailedAttempt(user);
    loginAttemptService.onSuccessfulLogin(user);
    assertEquals(0, loginAttemptService.getAttemptCount(user));
  }

  @Test
  void shouldNotLockOnFourthAttempt() {
    for (int i = 0; i < 4; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertFalse(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldLockOnFifthAttempt() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertTrue(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldRemainLockedWithLessThan15() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertTrue(loginAttemptService.isLocked(user));

    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(15));
    loginAttemptService.unlockIfEligible(user);
    assertTrue(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldUnlockWithMoreThan15() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertTrue(loginAttemptService.isLocked(user));

    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(16));

    loginAttemptService.unlockIfEligible(user);
    assertFalse(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldCreateLoginAttemptIfNotExists() {
    loginAttemptRepository.deleteAll();
    assertDoesNotThrow(
        () -> {
          loginAttemptService.registerFailedAttempt(user);
        });
    assertEquals(1, loginAttemptService.getAttemptCount(user));
  }

  @Test
  void shouldRemainLockedIfUnlockNotCalled() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(30));
    assertTrue(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldAllowRelockAfterUnlock() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(16));
    loginAttemptService.unlockIfEligible(user);

    assertFalse(loginAttemptService.isLocked(user));

    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertTrue(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldNotUnlockIfClockGoesBackwards() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    assertTrue(loginAttemptService.isLocked(user));

    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(-5));
    loginAttemptService.unlockIfEligible(user);

    assertTrue(loginAttemptService.isLocked(user));
  }

  @Test
  void shouldOnlyHaveOneLoginAttemptRecord() {
    loginAttemptService.registerFailedAttempt(user);
    loginAttemptService.registerFailedAttempt(user);
    assertEquals(1, loginAttemptRepository.findAll().size());
  }

  @Test
  void shouldBeIdempotentOnUnlockIfAlreadyUnlocked() {
    for (int i = 0; i < 5; i++) {
      loginAttemptService.registerFailedAttempt(user);
    }
    fixedClockTestConfig.setTime(clockService, Duration.ofMinutes(20));
    loginAttemptService.unlockIfEligible(user);
    loginAttemptService.unlockIfEligible(user);
    assertFalse(loginAttemptService.isLocked(user));
  }
}
