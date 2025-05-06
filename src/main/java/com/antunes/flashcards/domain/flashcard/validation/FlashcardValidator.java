package com.antunes.flashcards.domain.flashcard.validation;

import com.antunes.flashcards.domain.flashcard.model.Flashcard;

public class FlashcardValidator {
  public static boolean isValid(Flashcard flashcard) {
    return (flashcard.getQuestion() != null)
        && !flashcard.getQuestion().isBlank()
        && (flashcard.getAnswer() != null)
        && !flashcard.getAnswer().isBlank();
  }
}
