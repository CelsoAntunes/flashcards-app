package com.antunes.flashcards.domain.flascard.model;

import jakarta.persistence.*;

@Entity
public class Flashcard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String question;
  @Column private String answer;

  public Flashcard() {}

  public Flashcard(String question, String answer) {
    this.question = question;
    this.answer = answer;
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
}
