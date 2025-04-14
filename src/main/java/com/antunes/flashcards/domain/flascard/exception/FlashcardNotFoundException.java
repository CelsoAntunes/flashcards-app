package com.antunes.flashcards.domain.flascard.exception;

public class FlashcardNotFoundException extends RuntimeException {
  public FlashcardNotFoundException(String message) {
    super(message);
  }
}
