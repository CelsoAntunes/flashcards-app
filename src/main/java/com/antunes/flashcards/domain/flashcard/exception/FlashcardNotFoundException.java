package com.antunes.flashcards.domain.flashcard.exception;

public class FlashcardNotFoundException extends RuntimeException {
  public FlashcardNotFoundException(String message) {
    super(message);
  }
}
