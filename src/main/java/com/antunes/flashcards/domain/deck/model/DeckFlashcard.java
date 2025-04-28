package com.antunes.flashcards.domain.deck.model;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class DeckFlashcard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "deck_id")
  @Getter
  private Deck deck;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "flashcard_id")
  @Getter
  private Flashcard flashcard;

  @Getter @Setter private int positionInDeck;

  @CreationTimestamp private LocalDateTime addedAt;

  protected DeckFlashcard() {}

  private DeckFlashcard(Deck deck, Flashcard flashcard) {
    this.deck = deck;
    this.flashcard = flashcard;
  }

  public static DeckFlashcard link(Deck deck, Flashcard flashcard) {
    return link(deck, flashcard, 0);
  }

  public static DeckFlashcard link(Deck deck, Flashcard flashcard, int positionInDeck) {
    DeckFlashcard link = new DeckFlashcard(deck, flashcard);
    link.positionInDeck = positionInDeck;
    return link;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeckFlashcard deckFlashcard = (DeckFlashcard) o;
    return Objects.equals(id, deckFlashcard.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
