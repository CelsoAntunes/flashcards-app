package com.antunes.flashcards.domain.user.auth.reset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.PasswordValidator;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class PasswordResetUnitTests {
  private final String rawEmail = "user@example.com";
  private final String oldRawPassword = "oldPassword123";
  private final String newRawPassword = "newPassword123";
  private PasswordResetService passwordResetService;
  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  @Mock private PasswordValidator passwordValidator;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserRepository userRepository;
  @Mock private PasswordFactory passwordFactory;
  private JwtTokenProvider jwtTokenProvider;

  @Captor ArgumentCaptor<User> userCaptor;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
    passwordFactory = new PasswordFactory(passwordValidator, passwordEncoder);
    passwordResetService =
        new PasswordResetService(userRepository, passwordFactory, jwtTokenProvider);
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
    assertDoesNotThrow(
        () -> {
          passwordResetService.resetPassword(token, newRawPassword);
        });

    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertNotEquals(oldPassword.getHashedPassword(), savedUser.getHashedPassword());
    assertEquals("$2stub$" + newRawPassword, savedUser.getHashedPassword());
  }
}
