package com.antunes.flashcards.model;

import jakarta.persistence.*;

@Entity
public class Flashcard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String front;
  @Column private String back;

  public Flashcard() {}

  public Flashcard(String front, String back) {
    this.front = front;
    this.back = back;
  }

  public Long getId() {
    return id;
  }

  public String getFront() {
    return front;
  }

  public void setFront(String front) {
    this.front = front;
  }

  public String getBack() {
    return back;
  }

  public void setBack(String back) {
    this.back = back;
  }
}
