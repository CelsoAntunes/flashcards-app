package com.antunes.flashcards.domain.deck.model;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;

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

  @Getter private int positionInDeck;

  private LocalDateTime addedAt;

  private DeckFlashcard() {}

  private DeckFlashcard(Deck deck, Flashcard flashcard) {
    this.deck = deck;
    this.flashcard = flashcard;
    this.addedAt = LocalDateTime.now();
  }

  public static DeckFlashcard link(Deck deck, Flashcard flashcard) {
    return new DeckFlashcard(deck, flashcard);
  }

  public void unlink() {
    this.deck = null;
    this.flashcard = null;
  }
}
