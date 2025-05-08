package com.antunes.flashcards.domain.flashcard.model;

import com.antunes.flashcards.domain.flashcard.exception.FlashcardValidationException;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardWithoutUserException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;

@Getter
@Entity
public class Flashcard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String question;
  @Column private String answer;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  private User owner;

  public Flashcard() {}

  public Flashcard(String question, String answer, User owner) {
    this.question = question;
    this.answer = answer;
    this.owner = owner;
  }

  public static Flashcard create(String question, String answer, User owner) {
    if (question == null || question.isBlank()) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    if (answer == null || answer.isBlank()) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    if (owner == null) {
      throw new FlashcardWithoutUserException("User cannot be null");
    }
    if (owner.getId() == null) {
      throw new UserNotFoundException("Id cannot be null");
    }
    return new Flashcard(question, answer, owner);
  }

  public void changeQuestion(String newQuestion) {
    if (newQuestion == null || newQuestion.isBlank()) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    this.question = newQuestion.trim();
  }

  public void changeAnswer(String newAnswer) {
    if (newAnswer == null || newAnswer.isBlank()) {
      throw new FlashcardValidationException("Invalid flashcard");
    }
    this.answer = newAnswer.trim();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    Flashcard flashcard = (Flashcard) o;
    return Objects.equals(id, flashcard.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
