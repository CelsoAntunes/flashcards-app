package com.antunes.flashcards.domain.flascard.validation;

import com.antunes.flashcards.domain.flascard.model.Flashcard;

public class FlashcardValidator {
  public static boolean isValid(Flashcard flashcard) {
    return (flashcard.getQuestion() != null)
        && !flashcard.getQuestion().isBlank()
        && (flashcard.getAnswer() != null)
        && !flashcard.getAnswer().isBlank();
  }
}
