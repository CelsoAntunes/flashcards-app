package com.antunes.flashcards.domain.flashcard.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.service.FlashcardService;
import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class FlashcardServiceIntegrationTests {

  private static final String question = "question";
  private static final String answer = "answer";
  private static final String validationErrorInvalid = "Invalid flashcard";
  private static final String validationErrorNull = "Id cannot be null";
  private static final String notFoundError = "Flashcard not found";

  @Autowired private PasswordFactory passwordFactory;
  @Autowired private FlashcardService flashcardService;
  @Autowired private User user;

  @BeforeEach
  void setUp() {
    user = new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    PasswordValidator passwordValidator = new PasswordValidator();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    PasswordFactory passwordFactory = new PasswordFactory(passwordValidator, passwordEncoder);
    User validUser =
        new User(new Email("user@example.com"), passwordFactory.create("securePassword123"));
    return Stream.of(
        Arguments.of(" ", answer, validUser),
        Arguments.of("", answer, validUser),
        Arguments.of("   ", answer, validUser),
        Arguments.of(null, answer, validUser),
        Arguments.of(question, "", validUser),
        Arguments.of(question, " ", validUser),
        Arguments.of(question, "   ", validUser),
        Arguments.of(question, null, validUser),
        Arguments.of("", "", validUser),
        Arguments.of(null, null, validUser),
        Arguments.of(question, answer, null),
        Arguments.of(question, answer, 1),
        Arguments.of(null, null, null));
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
      Flashcard savedFlashcard = flashcardService.save(flashcard);
      assertFlashcardContent(savedFlashcard, question, answer, user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void saveInvalidFlashcard(String question, String answer, User owner) {
      Flashcard flashcard = new Flashcard(question, answer, owner);
      FlashcardValidationException exception =
          assertThrows(FlashcardValidationException.class, () -> flashcardService.save(flashcard));
      assertEquals(validationErrorInvalid, exception.getMessage());
    }
  }

  @Nested
  class FindById {
    @Test
    void findByIdValidId() {
      Flashcard flashcard = new Flashcard(question, answer, user);
      flashcardService.save(flashcard);
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
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, user);
      Flashcard updatedFlashcard =
          flashcardService.updateFlashcard(existingFlashcard, "new question", "new answer");
      assertFlashcardContent(existingFlashcard, "new question", "new answer", user);
      assertFlashcardContent(updatedFlashcard, "new question", "new answer", user);
    }

    @ParameterizedTest
    @MethodSource(
        "com.antunes.flashcards.domain.flashcard.service.FlashcardServiceIntegrationTests#provideInvalidFlashcardData")
    void updateFlashcardInvalidInput(String question, String answer, User owner) {
      Flashcard existingFlashcard = flashcardService.createFlashcard(question, answer, owner);
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
