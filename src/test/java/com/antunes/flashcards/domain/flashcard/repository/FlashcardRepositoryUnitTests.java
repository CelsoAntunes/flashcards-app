package com.antunes.flashcards.domain.flashcard.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FlashcardRepositoryUnitTests {

  @Mock private FlashcardRepository flashcardRepository;

  @Test
  void testSaveFlashcard() {
    Flashcard flashcard = new Flashcard("question", "answer");
    when(flashcardRepository.save(any(Flashcard.class))).thenReturn(flashcard);
    Flashcard savedFlashcard = flashcardRepository.save(flashcard);
    assertNotNull(savedFlashcard);
    assertEquals("question", flashcard.getQuestion());
    assertEquals("answer", flashcard.getAnswer());
    verify(flashcardRepository, times(1)).save(flashcard);
  }
}
