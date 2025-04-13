package com.antunes.flashcards.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EmailTest {

  @Test
  void validEmail_shouldNotThrow() {
    assertDoesNotThrow(() -> new Email("user@example.com"));
    assertEquals("user@example.com", Email.get());
  }

  @Test
  void invalidEmail_shouldThrow() {
    EmailValidationException exception =
        assertThrows(EmailValidationException.class, () -> new Email("not-an-email"));
    assertEquals("Not a valid email", exception.getMessage());
  }

  @Test
  void nullEmail_shouldThrow() {
    EmailValidationException exception =
        assertThrows(EmailValidationException.class, () -> new Email(null));
    assertEquals("Email cannot be null", exception.getMessage());
  }
}
