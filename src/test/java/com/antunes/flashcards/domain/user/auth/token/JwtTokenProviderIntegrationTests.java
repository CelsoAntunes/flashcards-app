package com.antunes.flashcards.domain.user.auth.token;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.TokenValidationException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class JwtTokenProviderIntegrationTests {
  @Autowired private JwtTokenProvider jwtTokenProvider;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FlashcardRepository flashcardRepository;

  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  private final String ExpiredTokenError = "Token has expired";
  private final String InvalidTokenError = "Invalid token";
  private final String NullOrBlankTokenError = "Token cannot be null or blank";

  @BeforeAll
  void setUp() {
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userService.register(rawEmail, rawPassword);
  }

  private String generateExpiredToken(String subject, TokenScenario scenario) {
    return Jwts.builder()
        .subject(subject)
        .claim("type", scenario.tokenType.name())
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
        .signWith(jwtTokenProvider.getSecretKey())
        .compact();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideTokenValidationCases")
  void shouldValidateTokenCorrectly(TokenValidationCase testCase) {
    if (testCase.expectsException()) {
      Exception ex =
          assertThrows(
              testCase.expectedException(),
              () ->
                  jwtTokenProvider.validateToken(testCase.token(), testCase.scenario().tokenType));
      assertEquals(testCase.expectedMessage(), ex.getMessage());
    } else {
      assertDoesNotThrow(
          () -> jwtTokenProvider.validateToken(testCase.token(), testCase.scenario().tokenType));
    }
  }

  Stream<TokenValidationCase> provideTokenValidationCases() {
    User user = userService.findByEmail(rawEmail).orElseThrow();

    Long userId = user.getId();
    String email = user.getEmail();

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
                        TokenValidationException.class,
                        InvalidTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Null Token",
                        scenario,
                        null,
                        TokenValidationException.class,
                        NullOrBlankTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Blank Token",
                        scenario,
                        "   ",
                        TokenValidationException.class,
                        NullOrBlankTokenError),
                    new TokenValidationCase(
                        scenario.name() + " - Expired Token",
                        scenario,
                        generateExpiredToken(email, scenario),
                        TokenExpiredException.class,
                        ExpiredTokenError)));
  }
}
