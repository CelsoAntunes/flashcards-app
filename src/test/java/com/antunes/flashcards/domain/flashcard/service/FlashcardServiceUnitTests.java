package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class FlashcardServiceUnitTests {

  public String validationErrorInvalid = "Invalid flashcard";
  public String validationErrorNull = "Id cannot be null";
  public String notFoundError = "Flashcard not found";

  @Mock private FlashcardRepository flashcardRepository;
  @Mock private PasswordFactory passwordFactory;
  @Mock private User user;

  @InjectMocks private FlashcardService flashcardService;

  @BeforeEach
  void setUp() {
    user = new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    PasswordValidator passwordValidator = new PasswordValidator();
    PasswordEncoder stubPasswordEncoder = new StubPasswordEncoder();
    PasswordFactory passwordFactory = new PasswordFactory(passwordValidator, stubPasswordEncoder);
    User validUser =
        new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
    return Stream.of(
        Arguments.of(" ", "answer", validUser),
        Arguments.of("", "answer", validUser),
        Arguments.of("   ", "answer", validUser),
        Arguments.of(null, "answer", validUser),
        Arguments.of("question", "", validUser),
        Arguments.of("question", " ", validUser),
        Arguments.of("question", "   ", validUser),
        Arguments.of("question", null, validUser),
        Arguments.of("", "", validUser),
        Arguments.of(null, null, validUser),
        Arguments.of("question", "answer", null),
        Arguments.of("question", "answer", 1),
        Arguments.of(null, null, null));
  }

  private Flashcard buildFlashcard(String question, String answer, User owner) {
    return new Flashcard(question, answer, owner);
  }

  private void assertFlashcardContent(
      Flashcard flashcard, String question, String answer, User owner) {
    assertNotNull(flashcard);
    assertEquals(question, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
    assertEquals(owner, flashcard.getOwner());
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
      Flashcard flashcard = buildFlashcard("question", "answer", user);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);

      Flashcard savedFlashcard = flashcardService.save(flashcard);

      assertFlashcardContent(savedFlashcard, "question", "answer", user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void saveInvalidInput(String question, String answer, User user) {
      Flashcard flashcard = buildFlashcard(question, answer, user);

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
      Flashcard flashcard = withId(new Flashcard("question", "answer", user), id);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
      when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
      flashcardRepository.save(flashcard);
      Flashcard foundById = flashcardService.findById(flashcard.getId());
      assertFlashcardContent(foundById, "question", "answer", user);
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

      Flashcard createdFlashcard = flashcardService.createFlashcard("question", "answer", user);
      assertFlashcardContent(createdFlashcard, "question", "answer", user);
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "question", "answer", user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void createFlashcardInvalidInput(String question, String answer, User owner) {
      FlashcardValidationException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.createFlashcard(question, answer, owner));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class UpdateFlashcard {
    @Test
    void updateFlashcardValidInput() {
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Flashcard existingFlashcard = buildFlashcard("question", "answer", user);
      assertNotNull(existingFlashcard);
      assertFlashcardContent(existingFlashcard, "question", "answer", user);

      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(updatedFlashcard, "new question", "new answer", user);
      assertFlashcardContent(existingFlashcard, "new question", "new answer", user);
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "new question", "new answer", user);
    }

    @Test
    void updateFlashcardWithSameData() {
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Flashcard existingFlashcard = buildFlashcard("question", "answer", user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "question", "answer");
      assertFlashcardContent(updatedFlashcard, "question", "answer", user);
      assertFlashcardContent(existingFlashcard, "question", "answer", user);
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, "question", "answer", user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String question, String answer, User owner) {
      Flashcard existingFlashcard = buildFlashcard("question", "answer", owner);
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
      Flashcard existingFlashcard = withId(new Flashcard("question", "answer", user), id);
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
