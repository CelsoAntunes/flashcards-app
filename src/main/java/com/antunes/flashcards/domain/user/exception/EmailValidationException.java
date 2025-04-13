package com.antunes.flashcards.domain.user.exception;

public class EmailValidationException extends RuntimeException {
  public EmailValidationException(String message) {
    super(message);
  }
}
