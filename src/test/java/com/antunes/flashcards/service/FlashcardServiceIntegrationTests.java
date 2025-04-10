package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;

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
  void saveValidFlashcard() {
    Flashcard flashcard = new Flashcard("front", "back");
    Flashcard savedFlashcard = flashcardService.save(flashcard);

    assertNotNull(savedFlashcard);
    assertEquals("front", savedFlashcard.getFront());
    assertEquals("back", savedFlashcard.getBack());
  }

  @Test
  void saveInvalidFlashcard() {
    Flashcard flashcard = new Flashcard(null, "back");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              flashcardService.save(flashcard);
            });
    assertNotNull(exception);
  }

  @Test
  void findByIdValidId() {
    Flashcard flashcard = new Flashcard("front", "back");
    flashcardService.save(flashcard);
    Flashcard retrievedFlashcard = flashcardService.findById(flashcard.getId());

    assertNotNull(retrievedFlashcard);
    assertEquals("front", retrievedFlashcard.getFront());
    assertEquals("back", retrievedFlashcard.getBack());
  }

  @Test
  void findByIdInvalidId() {
    Flashcard retrievedFlashcard = flashcardService.findById(999L);

    assertNull(retrievedFlashcard);
  }

  @Test
  void createFlashcardValidInput() {
    Flashcard createdFlashcard = flashcardService.createFlashcard("front", "back");

    assertNotNull(createdFlashcard);
    assertEquals("front", createdFlashcard.getFront());
    assertEquals("back", createdFlashcard.getBack());
  }
}
