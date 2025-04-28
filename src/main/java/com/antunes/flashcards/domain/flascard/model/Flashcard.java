package com.antunes.flashcards.domain.flascard.model;

import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class Flashcard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column @Setter private String question;
  @Column @Setter private String answer;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id")
  private User owner;

  public Flashcard() {}

  public Flashcard(String question, String answer, User owner) {
    this.question = question;
    this.answer = answer;
    this.owner = owner;
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
