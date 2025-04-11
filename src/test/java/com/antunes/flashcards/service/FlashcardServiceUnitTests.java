package com.antunes.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.exception.FlashcardNotFoundException;
import com.antunes.flashcards.exception.FlashcardValidationException;
import com.antunes.flashcards.model.Flashcard;
import com.antunes.flashcards.repository.FlashcardRepository;
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
      assertEquals("Invalid flashcard", exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = buildFlashcard("front", "back");
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

      assertEquals("Flashcard not found", exception.getMessage());
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
      assertEquals("Invalid flashcard", exception.getMessage());
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
      verify(flashcardRepository).save(existingFlashcard);
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
      assertEquals("Invalid flashcard", exception.getMessage());
    }
  }

  @Nested
  class DeleteFlashcard {
    @Test
    void deleteFlashcardExisting() {
      Flashcard existingFlashcard = buildFlashcard("front", "back");
      assertNotNull(existingFlashcard);
      assertFlashcardContent(existingFlashcard, "front", "back");
      when(flashcardRepository.findById(existingFlashcard.getId()))
          .thenReturn(Optional.of(existingFlashcard));
      flashcardService.deleteFlashcard(existingFlashcard);
      verify(flashcardRepository).delete(existingFlashcard);
    }

    @Test
    void deleteFlashcardNonExisting() {
      Flashcard fakeFlashcard = new Flashcard("front", "back");
      when(flashcardRepository.findById(fakeFlashcard.getId())).thenReturn(Optional.empty());
      FlashcardNotFoundException exception =
          assertThrows(
              FlashcardNotFoundException.class,
              () -> flashcardService.deleteFlashcard(fakeFlashcard));
      assertEquals("Flashcard not found", exception.getMessage());
    }
  }
}
