package com.antunes.flashcards.domain.user.auth.login;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class LoginServiceIntegrationTests {
  @Autowired private LoginService loginService;
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  @BeforeAll
  void setUp() {
    passwordResetTokenRepository.deleteAll();
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userService.register(rawEmail, rawPassword);
  }

  @BeforeEach
  void fetchUser() {
    User user = userService.findByEmail(rawEmail).get();
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
}
