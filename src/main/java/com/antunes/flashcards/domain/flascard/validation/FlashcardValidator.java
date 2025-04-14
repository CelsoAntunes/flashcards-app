package com.antunes.flashcards.domain.flascard.validation;

import com.antunes.flashcards.domain.flascard.model.Flashcard;

public class FlashcardValidator {
  public static boolean isValid(Flashcard flashcard) {
    return (flashcard.getFront() != null)
        && !flashcard.getFront().isBlank()
        && (flashcard.getBack() != null)
        && !flashcard.getBack().isBlank();
  }
}
