package com.antunes.flashcards.domain.user.model;

import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Email {
  private String rawEmail;

  protected Email() {}

  public Email(String rawEmail) {
    if (rawEmail == null) {
      throw new EmailValidationException("Email cannot be null");
    }
    if (!rawEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
      throw new EmailValidationException("Not a valid email");
    }
    this.rawEmail = rawEmail.toLowerCase();
  }

  public String getValue() {
    return rawEmail;
  }

  public String getUsername() {
    int atIndex = getValue().indexOf("@");
    return getValue().substring(0, atIndex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Email email = (Email) o;
    return Objects.equals(rawEmail, email.rawEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawEmail);
  }
}
