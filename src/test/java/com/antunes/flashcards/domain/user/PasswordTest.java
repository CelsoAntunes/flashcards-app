package com.antunes.flashcards.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PasswordTest {
  @Test
  void validPassword_shouldNotThrow() {
    assertDoesNotThrow(() -> new Password("securePassword123"));
  }

  @Test
  void passwordTooShort_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> new Password("short"));
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }

  @Test
  void passwordMissingUppercase_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> new Password("valid1234"));
    assertEquals("Password must contain an uppercase letter.", exception.getMessage());
  }

  @Test
  void passwordMissingDigit_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> new Password("InvalidPass"));
    assertEquals("Password must contain a digit.", exception.getMessage());
  }

  @Test
  void blankPassword_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> new Password("   "));
    assertEquals("Password must not be blank.", exception.getMessage());
  }

  @Test
  void nullPassword_shouldThrow() {
    PasswordValidationException exception =
        assertThrows(PasswordValidationException.class, () -> new Password(null));
    assertEquals("Password cannot be null", exception.getMessage());
  }
}
