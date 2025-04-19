package com.antunes.flashcards.domain.user.exception;

public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message);
  }
}
