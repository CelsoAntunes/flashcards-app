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

  public String validationErrorInvalid = "Invalid flashcard";
  public String validationErrorNull = "Id cannot be null";
  public String notFoundError = "Flashcard not found";

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
      FlashcardValidationException exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.save(flashcard));
      assertEquals(validationErrorInvalid, exception.getMessage());
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
      FlashcardNotFoundException exception =
          assertThrows(FlashcardNotFoundException.class, () -> flashcardService.findById(999L));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void findByIdNullId() {
      FlashcardValidationException exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.findById(null));
      assertEquals(validationErrorNull, exception.getMessage());
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
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.createFlashcard(front, back));
      assertEquals(validationErrorInvalid, exception.getMessage());
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
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.updateFlashcard(existingFlashcard, front, back));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class DeleteFlashcard {
    @Test
    void deleteFlashcardExistingId() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("front", "back");
      Long id = existingFlashcard.getId();
      flashcardService.deleteFlashcardById(id);
      FlashcardNotFoundException exception =
          assertThrows(FlashcardNotFoundException.class, () -> flashcardService.findById(id));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void deleteFlashcardExistingIdTwice() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("front", "back");
      Long id = existingFlashcard.getId();
      flashcardService.deleteFlashcardById(id);
      Exception exception =
          assertThrows(
              FlashcardNotFoundException.class, () -> flashcardService.deleteFlashcardById(id));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void deleteFlashcardNonExistingId() {
      Long fakeId = 999L;
      FlashcardNotFoundException exception =
          assertThrows(
              FlashcardNotFoundException.class, () -> flashcardService.deleteFlashcardById(fakeId));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void deleteFlashcardNullId() {
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class, () -> flashcardService.deleteFlashcardById(null));
      assertEquals(validationErrorNull, exception.getMessage());
    }
  }
}
