package com.antunes.flashcards.domain.user.exception;

public class TokenExpiredException extends RuntimeException {
  public TokenExpiredException(String message) {
    super(message);
  }
}
