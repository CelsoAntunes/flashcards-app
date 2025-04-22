package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FlashcardServiceUnitTests {

  private static final String question = "question";
  private static final String answer = "answer";

  public static final String validationErrorInvalid = "Invalid flashcard";
  public static final String validationErrorNull = "Id cannot be null";
  public static final String notFoundError = "Flashcard not found";
  public static final String nullUserError = "User cannot be null";

  @Mock private FlashcardRepository flashcardRepository;
  @Mock private PasswordFactory passwordFactory;
  @Mock private UserRepository userRepository;
  @Mock private User user;

  @InjectMocks private FlashcardService flashcardService;

  @BeforeEach
  void setUp() {
    Password mockPassword = new Password("securePassword123", new StubPasswordEncoder());
    when(passwordFactory.create(anyString())).thenReturn(mockPassword);
    user = new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    PasswordFactory passwordFactory = mock(PasswordFactory.class);
    Password mockPassword = new Password("securePassword123", new StubPasswordEncoder());
    when(passwordFactory.create(anyString())).thenReturn(mockPassword);
    User validUser =
        new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
    return Stream.of(
        Arguments.of(
            " ", answer, validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            "", answer, validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            "   ", answer, validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            null, answer, validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            question, "", validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            question, " ", validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            question, "   ", validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            question, null, validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of("", "", validUser, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(question, answer, null, FlashcardWithoutUserException.class, nullUserError));
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
      ReflectionTestUtils.setField(user, "id", 1L);
      Flashcard flashcard = buildFlashcard(question, answer, user);
      when(userRepository.existsById(1L)).thenReturn(true);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
      Flashcard savedFlashcard = flashcardService.validateAndSave(flashcard);
      assertFlashcardContent(savedFlashcard, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void saveInvalidInput(
        String question,
        String answer,
        User user,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {
      Flashcard flashcard = buildFlashcard(question, answer, user);

      RuntimeException exception =
          assertThrows(expectedException, () -> flashcardService.validateAndSave(flashcard));
      assertEquals(expectedMessage, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Long id = 1L;
      Flashcard flashcard = withId(new Flashcard(question, answer, user), id);
      when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
      when(flashcardRepository.findById(flashcard.getId())).thenReturn(Optional.of(flashcard));
      flashcardRepository.save(flashcard);
      Flashcard foundById = flashcardService.findById(flashcard.getId());
      assertFlashcardContent(foundById, question, answer, user);
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
      ReflectionTestUtils.setField(user, "id", 1L);
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(userRepository.existsById(1L)).thenReturn(true);
      Flashcard createdFlashcard = flashcardService.createFlashcard(question, answer, user);
      assertFlashcardContent(createdFlashcard, question, answer, user);
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void createFlashcardInvalidInput(
        String question,
        String answer,
        User owner,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {
      RuntimeException exception =
          assertThrows(
              expectedException, () -> flashcardService.createFlashcard(question, answer, owner));
      assertEquals(expectedMessage, exception.getMessage());
    }
  }

  @Nested
  class UpdateFlashcard {
    @Test
    void updateFlashcardValidInput() {
      ReflectionTestUtils.setField(user, "id", 1L);
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(userRepository.existsById(1L)).thenReturn(true);
      Flashcard existingFlashcard = buildFlashcard(question, answer, user);
      assertNotNull(existingFlashcard);
      assertFlashcardContent(existingFlashcard, question, answer, user);

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
      ReflectionTestUtils.setField(user, "id", 1L);
      when(flashcardRepository.save(any(Flashcard.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(userRepository.existsById(1L)).thenReturn(true);
      Flashcard existingFlashcard = buildFlashcard(question, answer, user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, question, answer);
      assertFlashcardContent(updatedFlashcard, question, answer, user);
      assertFlashcardContent(existingFlashcard, question, answer, user);
      ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
      verify(flashcardRepository).save(captor.capture());
      Flashcard captured = captor.getValue();
      assertFlashcardContent(captured, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceUnitTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(
        String question,
        String answer,
        User owner,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {
      Flashcard existingFlashcard =
          buildFlashcard(
              FlashcardServiceUnitTests.question, FlashcardServiceUnitTests.answer, owner);
      RuntimeException exception =
          assertThrows(
              expectedException,
              () -> flashcardService.updateFlashcard(existingFlashcard, question, answer));
      assertEquals(expectedMessage, exception.getMessage());
    }
  }

  @Nested
  class DeleteFlashcard {
    @Test
    void deleteFlashcardExistingId() {
      Long id = 1L;
      Flashcard existingFlashcard = withId(new Flashcard(question, answer, user), id);
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
