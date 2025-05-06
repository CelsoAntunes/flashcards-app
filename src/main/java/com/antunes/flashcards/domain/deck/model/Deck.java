package com.antunes.flashcards.domain.deck.model;

import com.antunes.flashcards.domain.deck.exception.DeckValidationException;
import com.antunes.flashcards.domain.deck.exception.ExistingFlashcardException;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;

@Entity
public class Deck {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Column @Getter private String title;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  @Getter
  private User owner;

  @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeckFlashcard> flashcardLinks = new ArrayList<>();

  private LocalDateTime createdAt;

  protected Deck() {}

  private Deck(String title, User owner) {
    this.title = title;
    this.owner = owner;
    this.createdAt = LocalDateTime.now();
  }

  public static Deck create(String title, User owner) {
    if (title == null || title.isBlank()) {
      throw new DeckValidationException("Title cannot be null or blank");
    }
    if (owner == null) {
      throw new UserNotFoundException("User does not exist");
    }
    return new Deck(title, owner);
  }

  public void addFlashcard(Flashcard flashcard) {
    if (this.hasFlashcard(flashcard)) {
      throw new ExistingFlashcardException("This flashcard already exists in this deck");
    }
    int nextPosition = flashcardLinks.size();
    this.flashcardLinks.add(DeckFlashcard.link(this, flashcard, nextPosition));
  }

  private Stream<DeckFlashcard> streamFlashcardLinksMatching(Flashcard flashcard) {
    return flashcardLinks.stream().filter(link -> link.getFlashcard().equals(flashcard));
  }

  public Optional<DeckFlashcard> findLinkWithFlashcard(Flashcard flashcard) {
    return streamFlashcardLinksMatching(flashcard).findFirst();
  }

  public boolean hasFlashcard(Flashcard flashcard) {
    return streamFlashcardLinksMatching(flashcard)
        .anyMatch(link -> link.getFlashcard().equals(flashcard));
  }

  public void removeFlashcardLink(DeckFlashcard deckFlashcard) {
    if (!this.flashcardLinks.contains(deckFlashcard)) {
      throw new FlashcardNotFoundException("Flashcard is not in the deck");
    }
    flashcardLinks.remove(deckFlashcard);
    for (int i = 0; i < flashcardLinks.size(); i++) {
      flashcardLinks.get(i).setPositionInDeck(i);
    }
  }

  public int size() {
    return flashcardLinks.size();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Deck deck = (Deck) o;
    return Objects.equals(id, deck.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
