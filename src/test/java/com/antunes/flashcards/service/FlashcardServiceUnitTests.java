package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.model.Flashcard;
import com.antunes.flashcards.repository.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FlashcardServiceUnitTests {

  @Mock private FlashcardRepository flashcardRepository;

  @InjectMocks private FlashcardService flashcardService;

  @Test
  void flashcardWithEmptyFieldsIsInvalid() {
    Flashcard flashcard = new Flashcard("", "");
    assertFalse(flashcardService.isValid(flashcard));
  }

  @Test
  void validFlashcard() {
    Flashcard flashcard = new Flashcard("front", "back");
    assertTrue(flashcardService.isValid(flashcard));
  }

  @Test
  void flashcardWithNullFront() {
    Flashcard flashcard = new Flashcard(null, "back");
    assertFalse(flashcardService.isValid(flashcard));
  }

  @Test
  void flashcardWithNullBack() {
    Flashcard flashcard = new Flashcard("front", null);
    assertFalse(flashcardService.isValid(flashcard));
  }

  @Test
  void flashcardWithWhitespaceFront() {
    Flashcard flashcard = new Flashcard(" ", "back");
    assertFalse(flashcardService.isValid(flashcard));
  }

  @Test
  void flashcardWithWhitespaceBack() {
    Flashcard flashcard = new Flashcard("front", "  ");
    assertFalse(flashcardService.isValid(flashcard));
  }
}
