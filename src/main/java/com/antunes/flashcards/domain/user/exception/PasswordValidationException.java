package com.antunes.flashcards.domain.user.exception;

public class PasswordValidationException extends RuntimeException {
  public PasswordValidationException(String message) {
    super(message);
  }
}
