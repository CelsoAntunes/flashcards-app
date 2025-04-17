package com.antunes.flashcards.domain.flashcard.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import org.junit.jupiter.api.Test;

public class FlashcardTest {

  @Test
  void shouldNotCreateFlashcardWithoutUser() {
    String question = "question";
    String answer = "answer";
    FlashcardWithoutUserException exception =
        assertThrows(
            FlashcardWithoutUserException.class, () -> new Flashcard(question, answer, null));
    assertEquals("Cannot create Flashcard without owner", exception.getMessage());
  }
}
