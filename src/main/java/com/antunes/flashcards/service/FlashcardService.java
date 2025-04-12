package com.antunes.flashcards.service;

import com.antunes.flashcards.exception.FlashcardNotFoundException;
import com.antunes.flashcards.exception.FlashcardValidationException;
import com.antunes.flashcards.model.Flashcard;
import com.antunes.flashcards.repository.FlashcardRepository;
import com.antunes.flashcards.validation.FlashcardValidator;
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
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public Flashcard findById(Long id) {
    if (id == null) {
      throw new FlashcardValidationException("Id cannot be null");
    }
    return flashcardRepository
        .findById(id)
        .orElseThrow(() -> new FlashcardNotFoundException("Flashcard not found"));
  }

  public Flashcard createFlashcard(String front, String back) {
    Flashcard flashcard = new Flashcard(front, back);
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public Flashcard updateFlashcard(Flashcard flashcard, String front, String back) {
    flashcard.setFront(front);
    flashcard.setBack(back);
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public void deleteFlashcardById(Long id) {
    if (id == null) {
      throw new FlashcardValidationException("Id cannot be null");
    }
    if (findById(id) == null) {
      throw new FlashcardNotFoundException("Flashcard not found");
    }
    flashcardRepository.deleteById(id);
  }
}
