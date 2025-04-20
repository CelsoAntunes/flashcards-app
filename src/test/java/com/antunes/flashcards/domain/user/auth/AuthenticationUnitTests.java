package com.antunes.flashcards.domain.user.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.exception.InvalidTokenException;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.infrastructure.security.JwtTokenProvider;
import com.antunes.flashcards.infrastructure.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationUnitTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  private final String ExpiredTokenError = "Token has expired";
  private final String InvalidTokenError = "Invalid token";
  private final String NullOrBlankTokenError = "Token cannot be null or blank";

  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  private LoginService loginService;

  @Mock UserRepository userRepository;
  private final PasswordEncoder passwordEncoder = new StubPasswordEncoder();
  private JwtTokenProvider jwtTokenProvider;

  String generateTokenWithExpiration(User user) {
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("type", "AUTH")
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(secretKey)
        .compact();
  }

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
    loginService = new LoginService(userRepository, passwordEncoder, jwtTokenProvider);
  }

  @Nested
  class Login {
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
      String incorrectPassword = "notThePassword";
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
          assertThrows(
              UserNotFoundException.class, () -> loginService.login(rawEmail, rawPassword));
      assertEquals("No accounts with this email", exception.getMessage());
    }

    @Test
    void login_shouldReturnValidJwtTokenContainingUserEmail() {
      Email email = new Email(rawEmail);
      Password mockedPassword = mock(Password.class);
      when(mockedPassword.getHashedPassword()).thenReturn("$2stub$" + rawPassword);
      User mockedUser = new User(email, mockedPassword);
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
      String token = loginService.login(rawEmail, rawPassword);
      Claims claims =
          Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
      assertEquals(rawEmail, claims.getSubject());
    }
  }

  @Nested
  class ValidateToken {
    @Test
    void expiredAuthToken_shouldThrow() {
      User mockedUser = new User(new Email(rawEmail), mock(Password.class));
      String expiredToken = generateTokenWithExpiration(mockedUser);

      TokenExpiredException exception =
          assertThrows(
              TokenExpiredException.class,
              () -> jwtTokenProvider.validateToken(expiredToken, TokenType.AUTH));
      assertEquals(ExpiredTokenError, exception.getMessage());
    }

    @Test
    void validAuthToken_shouldNotThrow() {
      User mockedUser = new User(new Email(rawEmail), mock(Password.class));
      String validToken =
          jwtTokenProvider.generateAuthToken(mockedUser.getEmail(), mockedUser.getId());

      assertDoesNotThrow(() -> jwtTokenProvider.validateToken(validToken, TokenType.AUTH));
    }

    @Test
    void invalidAuthToken_shouldThrow() {
      String invalidToken = "invalid.token";
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class,
              () -> jwtTokenProvider.validateToken(invalidToken, TokenType.AUTH));
      assertEquals(InvalidTokenError, exception.getMessage());
    }

    @Test
    void nullAuthToken_shouldThrow() {
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class,
              () -> jwtTokenProvider.validateToken(null, TokenType.AUTH));
      assertEquals(NullOrBlankTokenError, exception.getMessage());
    }

    @Test
    void blankAuthToken_shouldThrow() {
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class,
              () -> jwtTokenProvider.validateToken("   ", TokenType.AUTH));
      assertEquals(NullOrBlankTokenError, exception.getMessage());
    }
  }
}
