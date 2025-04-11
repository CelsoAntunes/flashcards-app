package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.exception.FlashcardNotFoundException;
import com.antunes.flashcards.exception.FlashcardValidationException;
import com.antunes.flashcards.model.Flashcard;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class FlashcardServiceIntegrationTests {

  @Autowired private FlashcardService flashcardService;

  private static Stream<Arguments> provideInvalidFlashcardData() {
    return Stream.of(
        Arguments.of(" ", "back"),
        Arguments.of("", "back"),
        Arguments.of("   ", "back"),
        Arguments.of(null, "back"),
        Arguments.of("front", ""),
        Arguments.of("front", " "),
        Arguments.of("front", "   "),
        Arguments.of("front", null),
        Arguments.of("", ""));
  }

  private void assertFlashcardContent(Flashcard flashcard, String front, String back) {
    assertNotNull(flashcard);
    assertEquals(front, flashcard.getFront());
    assertEquals(back, flashcard.getBack());
  }

  @Nested
  class Save {
    @Test
    void saveValidFlashcard() {
      Flashcard flashcard = new Flashcard("front", "back");
      Flashcard savedFlashcard = flashcardService.save(flashcard);

      assertFlashcardContent(savedFlashcard, "front", "back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void saveInvalidFlashcard(String front, String back) {
      Flashcard flashcard = new Flashcard(front, back);
      Exception exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.save(flashcard));
      assertNotNull(exception);
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = new Flashcard("front", "back");
      flashcardService.save(flashcard);
      Flashcard retrievedFlashcard = flashcardService.findById(flashcard.getId());

      assertFlashcardContent(retrievedFlashcard, "front", "back");
    }

    @Test
    void findByIdInvalidId() {
      Exception exception =
          assertThrows(FlashcardNotFoundException.class, () -> flashcardService.findById(999L));
      assertNotNull(exception);
    }
  }

  @Nested
  class CreateFlashcard {
    @Test
    void createFlashcardValidInput() {
      Flashcard createdFlashcard = flashcardService.createFlashcard("front", "back");
      assertFlashcardContent(createdFlashcard, "front", "back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void createFlashcardInvalidInput(String front, String back) {
      Exception exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.createFlashcard(front, back));
      assertNotNull(exception);
    }
  }

  @Nested
  class UpdateFlashcard {
    @Test
    void updateFlashcardValidInput() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("front", "back");
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new front", "new back");
      assertFlashcardContent(existingFlashcard, "new front", "new back");
      assertFlashcardContent(updatedFlashcard, "new front", "new back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String front, String back) {
      Flashcard existingFlashcard = flashcardService.createFlashcard("front", "back");
      Exception exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.updateFlashcard(existingFlashcard, front, back));
      assertNotNull(exception);
    }
  }
}
