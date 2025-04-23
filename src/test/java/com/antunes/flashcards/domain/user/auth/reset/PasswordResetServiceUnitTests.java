package com.antunes.flashcards.domain.user.auth.reset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.PasswordValidator;
import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.*;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceUnitTests {
  private final String rawEmail = "user@example.com";
  private final String randomEmail = "random@example.com";
  private final String oldRawPassword = "oldPassword123";
  private final String newRawPassword = "newPassword123";
  private PasswordResetService passwordResetService;
  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  private PasswordValidator passwordValidator;
  private PasswordFactory passwordFactory;
  private JwtTokenProvider jwtTokenProvider;

  public String generateExpiredResetToken(String subject) {
    return Jwts.builder()
        .subject(subject)
        .claim("type", TokenType.RESET)
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(secretKey)
        .compact();
  }

  @Captor ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
    passwordValidator = new PasswordValidator();
    passwordFactory = new PasswordFactory(passwordValidator, passwordEncoder);
    passwordResetService =
        new PasswordResetService(
            userRepository, passwordFactory, jwtTokenProvider, passwordResetTokenRepository);
  }

  @Test
  void registeredUserWithValidResetToken_shouldNotThrow() {
    Email email = new Email(rawEmail);
    User user = new User(email, mock(Password.class));
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    String token = passwordResetService.reset(rawEmail);
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token, TokenType.RESET));
  }

  @Test
  void unregisteredUser_shouldThrow() {
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> passwordResetService.reset(rawEmail));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void registeredUser_shouldReturnValidToken() {
    Email email = new Email(rawEmail);
    User user = new User(email, mock(Password.class));
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    String token = passwordResetService.reset(rawEmail);
    assertDoesNotThrow(
        () -> {
          Claims claims =
              Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
          assertEquals(rawEmail, claims.getSubject());
          assertEquals(TokenType.RESET.name(), claims.get("type"));
        });
  }

  @Test
  void registeredUserWithValidToken_shouldChangePassword() {
    Email email = new Email(rawEmail);
    when(passwordEncoder.encode(oldRawPassword)).thenReturn("$2stub$" + oldRawPassword);
    Password oldPassword = passwordFactory.create(oldRawPassword);
    User user = new User(email, oldPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(newRawPassword)).thenReturn("$2stub$" + newRawPassword);

    String token = passwordResetService.reset(rawEmail);
    PasswordResetToken resetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenRepository.findByToken(token))
        .thenReturn(Optional.ofNullable(resetToken));
    when(resetToken.isUsable()).thenReturn(true);

    EntityManager entityManager = mock(EntityManager.class);
    when(entityManager.merge(any())).thenAnswer(invocation -> invocation.getArgument(0));
    ReflectionTestUtils.setField(passwordResetService, "entityManager", entityManager);

    assertDoesNotThrow(
        () -> {
          passwordResetService.resetPassword(token, newRawPassword);
        });

    verify(entityManager).merge(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertNotEquals(oldPassword.getHashedPassword(), savedUser.getHashedPassword());
    assertEquals("$2stub$" + newRawPassword, savedUser.getHashedPassword());
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
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> passwordResetService.reset(randomEmail));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void invalidNewPassword_shouldThrow() {
    Email email = new Email(rawEmail);
    Password oldPassword = new Password(oldRawPassword, passwordEncoder);
    User user = new User(email, oldPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    String token = passwordResetService.reset(rawEmail);
    PasswordResetToken resetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenRepository.findByToken(token))
        .thenReturn(Optional.ofNullable(resetToken));
    when(resetToken.isUsable()).thenReturn(true);

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
    PasswordResetToken resetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenRepository.findByToken(token))
        .thenReturn(Optional.ofNullable(resetToken));
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
    String token = generateExpiredResetToken(rawEmail);
    PasswordResetToken resetToken = mock(PasswordResetToken.class);
    when(passwordResetTokenRepository.findByToken(token))
        .thenReturn(Optional.ofNullable(resetToken));
    when(resetToken.isUsable()).thenReturn(false);
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
    Email email = new Email(rawEmail);
    when(passwordEncoder.encode(oldRawPassword)).thenReturn("$2stub$" + oldRawPassword);
    Password oldPassword = passwordFactory.create(oldRawPassword);
    User user = new User(email, oldPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(newRawPassword)).thenReturn("$2stub$" + newRawPassword);

    String token = passwordResetService.reset(rawEmail);

    PasswordResetToken resetToken =
        new PasswordResetToken(user, token, Instant.now().plusSeconds(300));
    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

    EntityManager entityManager = mock(EntityManager.class);
    when(entityManager.merge(any())).thenAnswer(invocation -> invocation.getArgument(0));
    ReflectionTestUtils.setField(passwordResetService, "entityManager", entityManager);

    passwordResetService.resetPassword(token, newRawPassword);

    assertTrue(resetToken.isUsed());
  }
}
