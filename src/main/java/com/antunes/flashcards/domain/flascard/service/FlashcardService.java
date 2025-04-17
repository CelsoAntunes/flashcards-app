package com.antunes.flashcards.domain.flascard.service;

import com.antunes.flashcards.domain.flascard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flascard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flascard.validation.FlashcardValidator;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlashcardService {

  private final FlashcardRepository flashcardRepository;
  private final UserRepository userRepository;

  @Autowired
  public FlashcardService(FlashcardRepository flashcardRepository, UserRepository userRepository) {
    this.flashcardRepository = flashcardRepository;
    this.userRepository = userRepository;
  }

  public Flashcard validateAndSave(Flashcard flashcard) {
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    if (flashcard.getOwner() == null) {
      throw new FlashcardWithoutUserException("User cannot be null");
    }
    if (flashcard.getOwner().getId() == null) {
      throw new UserNotFoundException("User not found");
    }
    if (!userRepository.existsById(flashcard.getOwner().getId())) {
      throw new UserNotFoundException(
          "User with Id " + flashcard.getOwner().getId() + " not found");
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
    return validateAndSave(new Flashcard(question, answer, user));
  }

  public Flashcard updateFlashcard(Flashcard flashcard, String question, String answer) {
    flashcard.setQuestion(question);
    flashcard.setAnswer(answer);
    if (!FlashcardValidator.isValid(flashcard)) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    return validateAndSave(flashcard);
  }

  public void deleteFlashcardById(Long id) {
    if (id == null) {
      throw new FlashcardValidationException("Id cannot be null");
    }
    findById(id);
    flashcardRepository.deleteById(id);
  }
}
