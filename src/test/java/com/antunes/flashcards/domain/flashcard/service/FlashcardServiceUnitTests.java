package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FlashcardServiceUnitTests {

  public String validationErrorInvalid = "Invalid flashcard";
  public String validationErrorNull = "Id cannot be null";
  public String notFoundError = "Flashcard not found";

  @Mock private FlashcardRepository flashcardRepository;

  @InjectMocks private FlashcardService flashcardService;

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

  private Flashcard buildFlashcard(String question, String answer) {
    return new Flashcard(question, answer);
  }

  private void assertFlashcardContent(Flashcard flashcard, String question, String answer) {
    assertNotNull(flashcard);
    assertEquals(question, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
  }

  private Flashcard withId(Flashcard flashcard, Long id) {
    try {
      Field idField = Flashcard.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(flashcard, id);
      return flashcard;
    } catch (Exception e) {
      throw new RuntimeException("Failed to set ID via reflection", e);
    }
  }

  @Nested
  class Save {
    @Test
    void saveValidInput() {
      Flashcard flashcard = buildFlashcard("question", "answer");
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);

      Flashcard savedFlashcard = flashcardService.save(flashcard);

      assertFlashcardContent(savedFlashcard, "question", "answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void saveInvalidInput(String question, String answer) {
      Flashcard flashcard = buildFlashcard(question, answer);

      FlashcardValidationException exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.save(flashcard));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Long id = 1L;
      Flashcard flashcard = withId(new Flashcard("question", "answer"), id);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
      when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
      flashcardRepository.save(flashcard);
      Flashcard foundById = flashcardService.findById(flashcard.getId());
      assertFlashcardContent(foundById, "question", "answer");
    }

    @Test
    void findByIdInvalidId() {
      Long invalidId = 999L;
      when(flashcardRepository.findById(invalidId)).thenReturn(Optional.empty());
      FlashcardNotFoundException exception =
          assertThrows(
              FlashcardNotFoundException.class, () -> flashcardService.findById(invalidId));

      assertEquals(notFoundError, exception.getMessage());
    }
  }

  @Nested
  class CreateFlashcard {
    @Test
    void createFlashcardValidInput() {
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Flashcard createdFlashcard = flashcardService.createFlashcard("question", "answer");
      assertFlashcardContent(createdFlashcard, "question", "answer");
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "question", "answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
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
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Flashcard existingFlashcard = buildFlashcard("question", "answer");
      assertNotNull(existingFlashcard);
      assertFlashcardContent(existingFlashcard, "question", "answer");

      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(updatedFlashcard, "new question", "new answer");
      assertFlashcardContent(existingFlashcard, "new question", "new answer");
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "new question", "new answer");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String question, String answer) {
      Flashcard existingFlashcard = buildFlashcard("question", "answer");
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
      Long id = 1L;
      Flashcard existingFlashcard = withId(new Flashcard("question", "answer"), id);
      when(flashcardRepository.findById(existingFlashcard.getId()))
          .thenReturn(Optional.of(existingFlashcard));
      flashcardService.deleteFlashcardById(id);
      verify(flashcardRepository).findById(id);
      verify(flashcardRepository).deleteById(id);
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
