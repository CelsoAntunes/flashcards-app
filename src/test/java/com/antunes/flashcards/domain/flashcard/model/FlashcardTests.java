package com.antunes.flashcards.domain.flashcard.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.flashcard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class FlashcardTests {
  @Mock private PasswordFactory passwordFactory;

  private static final String question = "question";
  private static final String answer = "answer";
  private static final String newQuestion = "new question";
  private static final String newAnswer = "new answer";
  private static final String rawEmail = "user@example.com";
  private static final String rawPassword = "secretPassword123";
  private static final Long userId = 1L;

  private User user;

  private User createUserWithId(Long id) {
    User user = new User(new Email(rawEmail), mock(PasswordFactory.class).create(rawPassword));
    try {
      Field idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(user, id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set user id via reflection", e);
    }
    return user;
  }

  @BeforeAll
  void setUp() {
    user = createUserWithId(userId);
  }

  private static Stream<Arguments> provideInvalidFlashcardData() {
    FlashcardTests helper = new FlashcardTests(); // Because it's static
    User validUser = helper.createUserWithId(userId);
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
        Arguments.of(null, null, validUser));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidFlashcardData")
  void invalidFlashcard(String question, String answer, User owner) {
    FlashcardValidationException exception =
        assertThrows(
            FlashcardValidationException.class, () -> Flashcard.create(question, answer, owner));
    assertEquals("Invalid flashcard", exception.getMessage());
  }

  @Test
  void validFlashcard() {
    assertDoesNotThrow(() -> Flashcard.create(question, answer, user));
  }

  @Test
  void flashcardWithNullUserShouldThrow() {
    FlashcardWithoutUserException exception =
        assertThrows(
            FlashcardWithoutUserException.class, () -> Flashcard.create(question, answer, null));
    assertEquals("User cannot be null", exception.getMessage());
  }

  @Test
  void flashcardShouldStoreCorrectFields() {
    Flashcard flashcard = Flashcard.create(question, answer, user);
    assertEquals(question, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
    assertEquals(user, flashcard.getOwner());
  }

  @Test
  void changeQuestionShouldUpdateCorrectly() {
    Flashcard flashcard = Flashcard.create(question, answer, user);
    assertDoesNotThrow(() -> flashcard.changeQuestion(newQuestion));
    assertEquals(newQuestion, flashcard.getQuestion());
    assertEquals(answer, flashcard.getAnswer());
    assertEquals(user, flashcard.getOwner());
  }

  @Test
  void changeQuestionInvalidQuestionShouldThrow() {
    Flashcard flashcard = Flashcard.create(question, answer, user);
    FlashcardValidationException exception =
        assertThrows(FlashcardValidationException.class, () -> flashcard.changeQuestion(null));
    assertEquals("Invalid flashcard", exception.getMessage());
    exception =
        assertThrows(FlashcardValidationException.class, () -> flashcard.changeQuestion("    "));
    assertEquals("Invalid flashcard", exception.getMessage());
  }

  @Test
  void changeAnswerShouldUpdateCorrectly() {
    Flashcard flashcard = Flashcard.create(question, answer, user);
    assertDoesNotThrow(() -> flashcard.changeAnswer(newAnswer));
    assertEquals(question, flashcard.getQuestion());
    assertEquals(newAnswer, flashcard.getAnswer());
    assertEquals(user, flashcard.getOwner());
  }

  @Test
  void changeAnswerInvalidAnswerShouldThrow() {
    Flashcard flashcard = Flashcard.create(question, answer, user);
    FlashcardValidationException exception =
        assertThrows(FlashcardValidationException.class, () -> flashcard.changeAnswer(null));
    assertEquals("Invalid flashcard", exception.getMessage());
    exception =
        assertThrows(FlashcardValidationException.class, () -> flashcard.changeAnswer("    "));
    assertEquals("Invalid flashcard", exception.getMessage());
  }
}
