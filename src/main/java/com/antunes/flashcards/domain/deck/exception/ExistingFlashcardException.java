package com.antunes.flashcards.domain.deck.exception;

public class ExistingFlashcardException extends RuntimeException {
  public ExistingFlashcardException(String message) {
    super(message);
  }
}
