package com.antunes.flashcards.domain.flascard.exception;

public class FlashcardWithoutUserException extends RuntimeException {
  public FlashcardWithoutUserException(String message) {
    super(message);
  }
}
