package com.antunes.flashcards.exception;

public class FlashcardValidationException extends RuntimeException {
  private String errorCode;

  public FlashcardValidationException(String message) {
    super(message);
  }

  public FlashcardValidationException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
