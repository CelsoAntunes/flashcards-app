package com.antunes.flashcards.domain.user.auth.reset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordResetUnitTests {
  private final String rawEmail = "user@example.com";
  private PasswordResetService passwordResetService;
  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  @Mock private UserRepository userRepository;
  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
    passwordResetService = new PasswordResetService(userRepository, jwtTokenProvider);
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
}
