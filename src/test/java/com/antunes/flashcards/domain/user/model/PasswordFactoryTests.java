package com.antunes.flashcards.domain.user.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordFactoryTests {
  private static final PasswordFactory passwordFactory =
      new PasswordFactory(new PasswordValidator(), new BCryptPasswordEncoder());

  private static Stream<Arguments> provideInvalidPasswordData() {
    return Stream.of(
        Arguments.of("short", "Password must be at least 8 characters long"),
        Arguments.of("valid1234", "Password must contain an uppercase letter"),
        Arguments.of("VALID1234", "Password must contain a lowercase letter"),
        Arguments.of("InvalidPass", "Password must contain a number"),
        Arguments.of(null, "Password cannot be null"),
        Arguments.of(" ", "Password cannot be blank"));
  }

  @Test
  void validPassword_shouldNotThrow() {
    Password password = assertDoesNotThrow(() -> passwordFactory.create("securePassword123"));
    assertNotNull(password);
  }

  @ParameterizedTest
  @MethodSource(
      "com.antunes.flashcards.domain.user.model.PasswordFactoryTests#provideInvalidPasswordData")
  void invalidPassword_shouldThrow(String password, String expectedMessage) {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> passwordFactory.create(password));
    assertEquals(expectedMessage, exception.getMessage());
  }
}
