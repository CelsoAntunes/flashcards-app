package com.antunes.flashcards.domain.user.reset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.infrastructure.security.JwtTokenProvider;
import com.antunes.flashcards.infrastructure.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResetTokenTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";
  private final Long id = 1L;

  private final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  @Mock private UserRepository userRepository;
  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
  }

  @Test
  void shouldGenerateResetToken() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    User mockedUser = new User(email, mockedPassword);
    setField(mockedUser, "id", id);
    String token = jwtTokenProvider.generateResetToken(mockedUser.getEmail(), mockedUser.getId());
    assertNotNull(token);

    Claims claims = jwtTokenProvider.parseToken(token);
    assertEquals(rawEmail, claims.getSubject());
    assertEquals(TokenType.RESET.name(), claims.get("type"));
  }
}
