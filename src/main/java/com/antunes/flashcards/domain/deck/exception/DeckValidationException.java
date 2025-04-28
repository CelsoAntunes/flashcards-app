package com.antunes.flashcards.domain.deck.exception;

public class DeckValidationException extends RuntimeException {
  public DeckValidationException(String message) {
    super(message);
  }
}
