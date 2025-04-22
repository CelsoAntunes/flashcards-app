package com.antunes.flashcards.domain.user.auth.token;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.antunes.flashcards.domain.user.exception.InvalidTokenException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtTokenProviderUnitTests {
  private static final String ExpiredTokenError = "Token has expired";
  private static final String InvalidTokenError = "Invalid token";
  private static final String NullOrBlankTokenError = "Token cannot be null or blank";

  private static final SecretKey secretKey =
      Keys.hmacShaKeyFor("my-super-secret-key-that-is-32bytes!".getBytes(StandardCharsets.UTF_8));

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);
  }

  public static Stream<TokenValidationCase> tokenValidationCases() {
    String rawEmail = "user@example.com";
    User user = new User(new Email(rawEmail), mock(Password.class));
    Long userId = user.getId();
    String email = user.getEmail();
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecretKey(secretKey);

    return Stream.of(TokenScenario.values())
        .flatMap(
            scenario ->
                Stream.of(
                    new TokenValidationCase(
                        scenario.name() + " - Valid Token",
                        scenario,
                        scenario.generateToken(jwtTokenProvider, email, userId),
                        null,
                        null),
                    new TokenValidationCase(
                        scenario.name() + " - Invalid Token",
                        scenario,
                        "invalid.token",
                        InvalidTokenException.class,
                        InvalidTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Null Token",
                        scenario,
                        null,
                        InvalidTokenException.class,
                        NullOrBlankTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Blank Token",
                        scenario,
                        "   ",
                        InvalidTokenException.class,
                        NullOrBlankTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Expired Token",
                        scenario,
                        generateExpiredToken(email, scenario),
                        TokenExpiredException.class,
                        ExpiredTokenError)));
  }

  private static String generateExpiredToken(String subject, TokenScenario scenario) {
    return Jwts.builder()
        .subject(subject)
        .claim("type", scenario.tokenType.name()) // 💡 respect RESET/AUTH
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(secretKey)
        .compact();
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS) // ✅ needed to allow non-static @MethodSource
  class ValidateToken {
    @ParameterizedTest(name = "{0}")
    @MethodSource(
        "com.antunes.flashcards.domain.user.auth.token.JwtTokenProviderUnitTests#tokenValidationCases")
    void shouldValidateTokenCorrectly(TokenValidationCase testCase) {
      if (testCase.expectsException()) {
        Exception ex =
            assertThrows(
                testCase.expectedException(),
                () ->
                    jwtTokenProvider.validateToken(
                        testCase.token(), testCase.scenario().tokenType));
        assertEquals(testCase.expectedMessage(), ex.getMessage());
      } else {
        assertDoesNotThrow(
            () -> jwtTokenProvider.validateToken(testCase.token(), testCase.scenario().tokenType));
      }
    }
  }
}
