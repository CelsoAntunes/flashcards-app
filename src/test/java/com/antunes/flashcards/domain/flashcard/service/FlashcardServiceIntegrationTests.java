package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import java.util.function.Supplier;
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
  private static Supplier<User> validUserSupplier;

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
    validUserSupplier =
        () ->
            userService
                .findByEmail(rawEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    return Stream.of(
        Arguments.of(
            " ",
            answer,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            "",
            answer,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            "   ",
            answer,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            null,
            answer,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            question,
            "",
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            question,
            " ",
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            question,
            "   ",
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            question,
            null,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(
            "", "", validUserSupplier, FlashcardValidationException.class, validationErrorInvalid),
        Arguments.of(
            null,
            null,
            validUserSupplier,
            FlashcardValidationException.class,
            validationErrorInvalid),
        Arguments.of(question, answer, null, FlashcardWithoutUserException.class, nullUserError),
        Arguments.of(null, null, null, FlashcardValidationException.class, validationErrorInvalid));
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
  class Save {
    @Test
    void saveValidFlashcard() {
      Flashcard flashcard = new Flashcard(question, answer, user);
      Flashcard savedFlashcard = flashcardService.validateAndSave(flashcard);
      assertFlashcardContent(savedFlashcard, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void saveInvalidFlashcard(
        String question,
        String answer,
        Supplier<User> userSupplier,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {
      User owner = userSupplier == null ? null : userSupplier.get();
      Flashcard flashcard = new Flashcard(question, answer, owner);

      RuntimeException exception =
          assertThrows(expectedException, () -> flashcardService.validateAndSave(flashcard));
      assertEquals(expectedMessage, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = new Flashcard(question, answer, user);
      flashcardService.validateAndSave(flashcard);
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
        Supplier<User> userSupplier,
        Class<? extends RuntimeException> expectedException,
        String expectedMessage) {
      User owner = userSupplier == null ? null : userSupplier.get();
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
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(existingFlashcard, "new question", "new answer", user);
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

      FlashcardValidationException exception =
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
      assertFlashcardContent(existingFlashcard, question, answer, user);
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
