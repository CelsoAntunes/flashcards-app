package com.antunes.flashcards.service;

import com.antunes.flashcards.model.Flashcard;
import com.antunes.flashcards.repository.FlashcardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlashcardService {

  private final FlashcardRepository flashcardRepository;

  @Autowired
  public FlashcardService(FlashcardRepository flashcardRepository) {
    this.flashcardRepository = flashcardRepository;
  }

  public Flashcard save(Flashcard flashcard) {
    if (!isValid(flashcard)) {
      throw new IllegalArgumentException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public Flashcard findById(Long id) {
    return flashcardRepository.findById(id).orElse(null);
  }

  public Flashcard createFlashcard(String front, String back) {
    Flashcard flashcard = new Flashcard(front, back);
    if (!isValid(flashcard)) {
      throw new IllegalArgumentException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public boolean isValid(Flashcard flashcard) {
    return (flashcard.getFront() != null)
        && !flashcard.getFront().isBlank()
        && (flashcard.getBack() != null)
        && !flashcard.getBack().isBlank();
  }
}
