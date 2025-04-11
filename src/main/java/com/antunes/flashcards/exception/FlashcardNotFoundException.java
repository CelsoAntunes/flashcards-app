package com.antunes.flashcards.exception;

public class FlashcardNotFoundException extends RuntimeException {
  public FlashcardNotFoundException(String message) {
    super(message);
  }
}
