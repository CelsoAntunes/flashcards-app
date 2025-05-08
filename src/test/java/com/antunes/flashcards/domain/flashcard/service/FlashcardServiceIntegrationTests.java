package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flashcard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class FlashcardServiceIntegrationTests {
  private static final String question = "question";
  private static final String answer = "answer";
  private static final String validationErrorInvalid = "Invalid flashcard";
  private static final String validationErrorNull = "Id cannot be null";
  private static final String notFoundError = "Flashcard not found";
  private static final String nullUserError = "User cannot be null";

  @Autowired private FlashcardService flashcardService;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

  private User user;

  @BeforeAll
  void setUp() {
    final String rawEmail = "user@example.com";
    final String rawPassword = "securePassword123";
    passwordResetTokenRepository.deleteAll();
    userRepository.deleteAll();
    user = userService.register(rawEmail, rawPassword);
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    return Stream.of(
        Arguments.of(" ", answer, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of("", answer, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of("   ", answer, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(null, answer, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(question, "", FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(question, " ", FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(question, "   ", FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(question, null, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of("", "", FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(null, null, FlashcardValidationException.class, validationErrorInvalid));
  }

  private static Stream<Arguments> provideInvalidDataUpdate() {
    return Stream.of(
        Arguments.of(" ", answer),
        Arguments.of("", answer),
        Arguments.of("   ", answer),
        Arguments.of(null, answer),
        Arguments.of(question, ""),
        Arguments.of(question, " "),
        Arguments.of(question, "   "),
        Arguments.of(question, null),
        Arguments.of("", ""),
        Arguments.of(null, null));
  }

  private void assertFlashcardContent(
      Flashcard flashcard, String question, String answer, User owner) {
    assertNotNull(flashcard);
    assertEquals(question, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
    assertEquals(owner, flashcard.getOwner());
  }

  @Nested
  class CreateFlashcard {
    @Test
    void createFlashcardValidInput() {
      Flashcard createdFlashcard = flashcardService.createFlashcard(question, answer, user);
      assertFlashcardContent(createdFlashcard, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void createFlashcardInvalidInput(
        String question,
        String answer,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {

      RuntimeException exception =
          assertThrows(
              expectedException, () -> flashcardService.createFlashcard(question, answer, user));
      assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void createFlashcardNullUser() {
      RuntimeException exception =
          assertThrows(
              FlashcardWithoutUserException.class,
              () -> flashcardService.createFlashcard(question, answer, null));
      assertEquals(nullUserError, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = flashcardService.createFlashcard(question, answer, user);
      Flashcard retrievedFlashcard = flashcardService.findById(flashcard.getId());
      assertFlashcardContent(retrievedFlashcard, question, answer, user);
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
  class UpdateFlashcard {
    @Test
    void updateFlashcardValidInput() {
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(updatedFlashcard, "new question", "new answer", user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidDataUpdate")
    void updateFlashcardInvalidInput(String question, String answer) {
      Flashcard existingFlashcard =
          flashcardService.createFlashcard(
              FlashcardServiceIntegrationTests.question,
              FlashcardServiceIntegrationTests.answer,
              user);

      RuntimeException exception =
          assertThrows(
              FlashcardValidationException.class,
              () -> flashcardService.updateFlashcard(existingFlashcard, question, answer));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }

    @Test
    void updateFlashcardWithSameData() {
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, question, answer);
      assertFlashcardContent(updatedFlashcard, question, answer, user);
    }
  }

  @Nested
  class DeleteFlashcard {
    @Test
    void deleteFlashcardExistingId() {
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
      Long id = existingFlashcard.getId();
      flashcardService.deleteFlashcardById(id);
      FlashcardNotFoundException exception =
          assertThrows(FlashcardNotFoundException.class, () -> flashcardService.findById(id));
      assertEquals(notFoundError, exception.getMessage());
    }

    @Test
    void deleteFlashcardExistingIdTwice() {
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
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
