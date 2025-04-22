package com.antunes.flashcards.domain.user.exception;

public class TokenValidationException extends RuntimeException {
  public TokenValidationException(String message) {
    super(message);
  }
}
