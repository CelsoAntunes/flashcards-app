package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.exception.FlashcardNotFoundException;
import com.antunes.flashcards.exception.FlashcardValidationException;
import com.antunes.flashcards.model.Flashcard;
import com.antunes.flashcards.repository.FlashcardRepository;
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

  private Flashcard buildFlashcard(String front, String back) {
    return new Flashcard(front, back);
  }

  private void assertFlashcardContent(Flashcard flashcard, String front, String back) {
    assertNotNull(flashcard);
    assertEquals(front, flashcard.getFront());
    assertEquals(back, flashcard.getBack());
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
      Flashcard flashcard = buildFlashcard("front", "back");
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);

      Flashcard savedFlashcard = flashcardService.save(flashcard);

      assertFlashcardContent(savedFlashcard, "front", "back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void saveInvalidInput(String front, String back) {
      Flashcard flashcard = buildFlashcard(front, back);

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
      Flashcard flashcard = withId(new Flashcard("front", "back"), id);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
      when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
      flashcardRepository.save(flashcard);
      Flashcard foundById = flashcardService.findById(flashcard.getId());
      assertFlashcardContent(foundById, "front", "back");
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

      Flashcard createdFlashcard = flashcardService.createFlashcard("front", "back");
      assertFlashcardContent(createdFlashcard, "front", "back");
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "front", "back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
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
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Flashcard existingFlashcard = buildFlashcard("front", "back");
      assertNotNull(existingFlashcard);
      assertFlashcardContent(existingFlashcard, "front", "back");

      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new front", "new back");
      assertFlashcardContent(updatedFlashcard, "new front", "new back");
      assertFlashcardContent(existingFlashcard, "new front", "new back");
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "new front", "new back");
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String front, String back) {
      Flashcard existingFlashcard = buildFlashcard("front", "back");
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
      Long id = 1L;
      Flashcard existingFlashcard = withId(new Flashcard("front", "back"), id);
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
