package com.antunes.flashcards.domain.flashcard.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.validation.FlashcardValidator;
import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

public class FlashcardValidatorUnitTests {
  @Mock private PasswordFactory passwordFactory;

  private static final String question = "question";
  private static final String answer = "answer";
  private static final String rawEmail = "user@example.com";
  private static final String rawPassword = "secretPassword123";

  private static Stream<Arguments> provideInvalidFlashcardData() {
    PasswordFactory passwordFactory = mock(PasswordFactory.class);
    Password mockPassword = new Password("securePassword123", new StubPasswordEncoder());
    when(passwordFactory.create(anyString())).thenReturn(mockPassword);
    User validUser = new User(new Email(rawEmail), passwordFactory.create(rawPassword));
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
    Flashcard flashcard = new Flashcard(question, answer, owner);
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void validFlashcard() {
    User user = new User(new Email(rawEmail), mock(PasswordFactory.class).create(rawPassword));
    Flashcard flashcard = new Flashcard(question, answer, user);
    assertTrue(FlashcardValidator.isValid(flashcard));
  }
}
