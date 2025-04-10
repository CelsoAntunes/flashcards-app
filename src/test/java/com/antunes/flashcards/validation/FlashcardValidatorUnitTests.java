package com.antunes.flashcards.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.model.Flashcard;
import org.junit.jupiter.api.Test;

public class FlashcardValidatorUnitTests {

  @Test
  void flashcardWithEmptyFieldsIsInvalid() {
    Flashcard flashcard = new Flashcard("", "");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void validFlashcard() {
    Flashcard flashcard = new Flashcard("front", "back");
    assertTrue(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void flashcardWithNullFront() {
    Flashcard flashcard = new Flashcard(null, "back");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void flashcardWithNullBack() {
    Flashcard flashcard = new Flashcard("front", null);
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void flashcardWithWhitespaceFront() {
    Flashcard flashcard = new Flashcard(" ", "back");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void flashcardWithWhitespaceBack() {
    Flashcard flashcard = new Flashcard("front", "  ");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }
}
