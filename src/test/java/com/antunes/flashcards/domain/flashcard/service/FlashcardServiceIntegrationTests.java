package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
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
        Arguments.of(" ", "answer"),
        Arguments.of("", "answer"),
        Arguments.of("   ", "answer"),
        Arguments.of(null, "answer"),
        Arguments.of("question", ""),
        Arguments.of("question", " "),
        Arguments.of("question", "   "),
        Arguments.of("question", null),
        Arguments.of("", ""));
  }

  private void assertFlashcardContent(Flashcard flashcard, String question, String answer) {
    assertNotNull(flashcard);
    assertEquals(question, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
  }

  @Nested
  class Save {
    @Test
    void saveValidFlashcard() {
      Flashcard flashcard = new Flashcard("question", "answer");
      Flashcard savedFlashcard = flashcardService.save(flashcard);
      assertFlashcardContent(savedFlashcard, "question", "answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void saveInvalidFlashcard(String question, String answer) {
      Flashcard flashcard = new Flashcard(question, answer);
      FlashcardValidationException exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.save(flashcard));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = new Flashcard("question", "answer");
      flashcardService.save(flashcard);
      Flashcard retrievedFlashcard = flashcardService.findById(flashcard.getId());

      assertFlashcardContent(retrievedFlashcard, "question", "answer");
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
      Flashcard createdFlashcard = flashcardService.createFlashcard("question", "answer");
      assertFlashcardContent(createdFlashcard, "question", "answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void createFlashcardInvalidInput(String question, String answer) {
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.createFlashcard(question, answer));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class UpdateFlashcard {
    @Test
    void updateFlashcardValidInput() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("question", "answer");
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(updatedFlashcard, "new question", "new answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String question, String answer) {
      Flashcard existingFlashcard = flashcardService.createFlashcard("question", "answer");
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.updateFlashcard(existingFlashcard, question, answer));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class DeleteFlashcard {
    @Test
    void deleteFlashcardExistingId() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("question", "answer");
      Long id = existingFlashcard.getId();
      flashcardService.deleteFlashcardById(id);
      FlashcardNotFoundException exception =
          assertThrows(FlashcardNotFoundException.class, () -> flashcardService.findById(id));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void deleteFlashcardExistingIdTwice() {
      Flashcard existingFlashcard = flashcardService.createFlashcard("question", "answer");
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
