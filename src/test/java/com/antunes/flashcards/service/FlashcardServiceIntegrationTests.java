package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.antunes.flashcards.model.Flashcard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class FlashcardServiceIntegrationTests {

  @Autowired private FlashcardService flashcardService;

  @Test
  void shouldCreateFlashcard() {
    Flashcard flashcard = new Flashcard("front", "back");

    Flashcard savedFlashcard = flashcardService.save(flashcard);

    assertNotNull(savedFlashcard);
    assertEquals("front", savedFlashcard.getFront());
    assertEquals("back", savedFlashcard.getBack());

    Flashcard retrievedFlashcard = flashcardService.findById(flashcard.getId());

    assertNotNull(retrievedFlashcard);
    assertEquals("front", retrievedFlashcard.getFront());
    assertEquals("back", retrievedFlashcard.getBack());
  }
}
