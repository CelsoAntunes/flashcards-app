package com.antunes.flashcards.domain.user.exception;

public class ResetTokenNotFoundException extends RuntimeException {
  public ResetTokenNotFoundException(String message) {
    super(message);
  }
}
