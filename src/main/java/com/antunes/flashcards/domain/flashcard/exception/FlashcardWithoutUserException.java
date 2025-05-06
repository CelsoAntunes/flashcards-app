package com.antunes.flashcards.domain.flashcard.exception;

public class FlashcardWithoutUserException extends RuntimeException {
  public FlashcardWithoutUserException(String message) {
    super(message);
  }
}
