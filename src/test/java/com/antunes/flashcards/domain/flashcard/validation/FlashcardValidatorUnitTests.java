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

  @ParameterizedTest
  @MethodSource("provideInvalidFlashcardData")
  void invalidFlashcard() {
    Flashcard flashcard = new Flashcard("", "");
    assertFalse(FlashcardValidator.isValid(flashcard));
  }

  @Test
  void validFlashcard() {
    Flashcard flashcard = new Flashcard("front", "back");
    assertTrue(FlashcardValidator.isValid(flashcard));
  }
}
