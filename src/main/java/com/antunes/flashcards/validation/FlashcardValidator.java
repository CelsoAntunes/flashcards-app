package com.antunes.flashcards.validation;

import com.antunes.flashcards.model.Flashcard;

public class FlashcardValidator {
  public static boolean isValid(Flashcard flashcard) {
    return (flashcard.getFront() != null)
        && !flashcard.getFront().isBlank()
        && (flashcard.getBack() != null)
        && !flashcard.getBack().isBlank();
  }
}
