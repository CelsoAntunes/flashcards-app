package com.antunes.flashcards.domain.flascard.service;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flascard.validation.FlashcardValidator;
import com.antunes.flashcards.domain.user.model.User;
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

  public Flashcard createFlashcard(String question, String answer, User user) {
    Flashcard flashcard = new Flashcard(question, answer, user);
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    return flashcardRepository.save(flashcard);
  }

  public Flashcard updateFlashcard(Flashcard flashcard, String question, String answer) {
    flashcard.setQuestion(question);
    flashcard.setAnswer(answer);
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
