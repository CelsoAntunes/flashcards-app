package com.antunes.flashcards.domain.user.exception;

public class UserTimeoutException extends RuntimeException {
  public UserTimeoutException(String message) {
    super(message);
  }
}
