package com.antunes.flashcards.domain.user.exception;

public class ExistingEmailException extends RuntimeException {
  public ExistingEmailException(String message) {
    super(message);
  }
}
