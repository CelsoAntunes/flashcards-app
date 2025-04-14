package com.antunes.flashcards.domain.user.model;

import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {
  private final String value;

  public Email(String value) {
    if (value == null) {
      throw new EmailValidationException("Email cannot be null");
    }
    if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
      throw new EmailValidationException("Not a valid email");
    }
    this.value = value.toLowerCase();
  }

  public String getValue() {
    return value;
  }
}
