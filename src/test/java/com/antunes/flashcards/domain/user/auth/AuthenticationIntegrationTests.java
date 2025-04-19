package com.antunes.flashcards.domain.user.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.exception.InvalidTokenException;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import com.antunes.flashcards.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationIntegrationTests {
  @Autowired private LoginService loginService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FlashcardRepository flashcardRepository;

  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  private final String ExpiredTokenError = "Token has expired";
  private final String InvalidTokenError = "Invalid token";
  private final String NullOrBlankTokenError = "Token cannot be null or blank";

  String generateTokenWithExpiration(User user) {
    return Jwts.builder()
        .subject(user.getEmail())
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(jwtTokenProvider.getSecretKey())
        .compact();
  }

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

  @Nested
  class Login {
    @Test
    void registeredUserCanLoginCorrectPassword() {
      String token = loginService.login(rawEmail, rawPassword);
      assertNotNull(token);
    }

    @Test
    void registeredUserCannotLoginIncorrectPassword_shouldThrow() {
      String incorrectPassword = "notThePassword";
      PasswordValidationException exception =
          assertThrows(
              PasswordValidationException.class,
              () -> loginService.login(rawEmail, incorrectPassword));
      assertEquals("Incorrect password", exception.getMessage());
    }
  }

  @Test
  void unregisteredUserCannotLogin_shouldThrow() {
    String notRegisteredEmail = "notthere@example.com";
    UserNotFoundException exception =
        assertThrows(
            UserNotFoundException.class, () -> loginService.login(notRegisteredEmail, rawPassword));
    assertEquals("No accounts with this email", exception.getMessage());
  }

  @Test
  void login_shouldReturnValidJwtTokenContainingUserEmail() {
    String token = loginService.login(rawEmail, rawPassword);
    Claims claims =
        Jwts.parser()
            .verifyWith(jwtTokenProvider.getSecretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    assertEquals(rawEmail, claims.getSubject());
  }

  @Nested
  class ValidateToken {
    @Test
    void expiredToken_shouldThrow() {
      String token = generateTokenWithExpiration(user);
      TokenExpiredException exception =
          assertThrows(TokenExpiredException.class, () -> jwtTokenProvider.validateToken(token));
      assertEquals(ExpiredTokenError, exception.getMessage());
    }

    @Test
    void validToken_shouldNotThrow() {
      String validToken = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
      assertDoesNotThrow(() -> jwtTokenProvider.validateToken(validToken));
    }

    @Test
    void invalidToken_shouldThrow() {
      String invalidToken = "invalid.token";
      InvalidTokenException exception =
          assertThrows(
              InvalidTokenException.class, () -> jwtTokenProvider.validateToken(invalidToken));
      assertEquals(InvalidTokenError, exception.getMessage());
    }

    @Test
    void nullToken_shouldThrow() {
      InvalidTokenException exception =
          assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken(null));
      assertEquals(NullOrBlankTokenError, exception.getMessage());
    }

    @Test
    void blankToken_shouldThrow() {
      InvalidTokenException exception =
          assertThrows(InvalidTokenException.class, () -> jwtTokenProvider.validateToken("  "));
      assertEquals(NullOrBlankTokenError, exception.getMessage());
    }
  }
}
