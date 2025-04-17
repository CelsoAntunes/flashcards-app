package com.antunes.flashcards.domain.flashcard.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.validation.FlashcardValidator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FlashcardValidatorUnitTests {

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

  @ParameterizedTest
  @MethodSource("provideInvalidFlashcardData")
  void invalidFlashcard() {
    Flashcard flashcard = new Flashcard("", "");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void validFlashcard() {
    Flashcard flashcard = new Flashcard("question", "answer");
    assertTrue(FlashcardValidator.isValid(flashcard));
  }
}
