package com.antunes.flashcards.domain.user.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import org.junit.jupiter.api.Test;

public class EmailTests {

  @Test
  void validEmail_shouldNotThrow() {
    Email email = new Email("user@example.com");
    assertEquals("user@example.com", email.getValue());
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
