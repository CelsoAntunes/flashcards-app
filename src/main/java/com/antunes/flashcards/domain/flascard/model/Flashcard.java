package com.antunes.flashcards.domain.flascard.model;

import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;

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

  public Long getId() {
    return id;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public User getOwner() {
    return owner;
  }
}
