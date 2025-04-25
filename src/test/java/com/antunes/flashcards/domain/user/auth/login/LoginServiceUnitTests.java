package com.antunes.flashcards.domain.user.auth.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.exception.UserTimeoutException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class LoginServiceUnitTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";
  private final String incorrectPassword = "notThePassword";

  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  private LoginService loginService;

  @Mock UserRepository userRepository;
  @Mock private LoginAttemptService loginAttemptService;

  private final PasswordEncoder passwordEncoder = new StubPasswordEncoder();

  @BeforeEach
  void setUp() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
    loginService =
        new LoginService(userRepository, passwordEncoder, jwtTokenProvider, loginAttemptService);
  }

  @Test
  void registeredUserCanLoginCorrectPassword() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    String token = loginService.login(rawEmail, rawPassword);
    assertNotNull(token);
  }

  @Test
  void registeredUserCannotLoginIncorrectPassword_shouldThrow() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class,
            () -> loginService.login(rawEmail, incorrectPassword));
    assertEquals("Incorrect password", exception.getMessage());
  }

  @Test
  void unregisteredUserCannotLogin_shouldThrow() {
    Email email = new Email(rawEmail);
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> loginService.login(rawEmail, rawPassword));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void loginShouldReturnValidJwtTokenContainingUserEmail() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    String token = loginService.login(rawEmail, rawPassword);
    assertDoesNotThrow(
        () -> {
          Claims claims =
              Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
          assertEquals(rawEmail, claims.getSubject());
          assertEquals(TokenType.AUTH.name(), claims.get("type"));
        });
  }

  @Test
  void onFiveFailedLoginAttempts_shouldLock() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    when(loginAttemptService.isLocked(mockedUser)).thenReturn(false);

    for (int i = 0; i < 5; i++) {
      assertThrows(
          PasswordValidationException.class, () -> loginService.login(rawEmail, incorrectPassword));
    }

    when(loginAttemptService.isLocked(mockedUser)).thenReturn(true);

    UserTimeoutException exception =
        assertThrows(
            UserTimeoutException.class, () -> loginService.login(rawEmail, incorrectPassword));
    assertEquals("Your account was timedout for 15 minutes", exception.getMessage());
  }

  @Test
  void onSuccessfulLogin_shouldResetCount() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    when(loginAttemptService.isLocked(mockedUser)).thenReturn(false);

    for (int i = 0; i < 4; i++) {
      assertThrows(
          PasswordValidationException.class, () -> loginService.login(rawEmail, incorrectPassword));
    }

    assertDoesNotThrow(() -> loginService.login(rawEmail, rawPassword));
    verify(loginAttemptService).onSuccessfulLogin(mockedUser);
  }

  @Test
  void shouldUnlockAfter15Minutes() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));

    // Simulate locked but eligible for unlock
    when(loginAttemptService.isLocked(mockedUser)).thenReturn(true).thenReturn(false);
    doAnswer(
            invocation -> {
              when(loginAttemptService.isLocked(mockedUser)).thenReturn(false);
              return null;
            })
        .when(loginAttemptService)
        .unlockIfEligible(mockedUser);

    assertDoesNotThrow(() -> loginService.login(rawEmail, rawPassword));
    verify(loginAttemptService).onSuccessfulLogin(mockedUser);
  }

  @Test
  void multipleSuccessfulLogins_shouldAlwaysSucceed() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
    User mockedUser = new User(email, mockedPassword);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    when(loginAttemptService.isLocked(mockedUser)).thenReturn(false);

    for (int i = 0; i < 3; i++) {
      String token = loginService.login(rawEmail, rawPassword);
      assertNotNull(token);
    }

    verify(loginAttemptService, times(3)).onSuccessfulLogin(mockedUser);
  }
}
