package com.antunes.flashcards.domain.deck.model;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Entity
public class Deck {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Column @Getter private String title;

  @Column
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  @Getter
  private User owner;

  @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeckFlashcard> flashcardLinks = new ArrayList<>();

  private LocalDateTime createdAt;

  private Deck() {}

  private Deck(String title, User owner) {
    this.title = title;
    this.owner = owner;
    this.createdAt = LocalDateTime.now();
  }

  public static Deck create(String title, User owner) {
    return new Deck(title, owner);
  }

  public void addFlashcard(Flashcard flashcard) {
    this.flashcardLinks.add(DeckFlashcard.link(this, flashcard));
  }

  public Optional<DeckFlashcard> findLinkWithFlashcard(Flashcard flashcard) {
    return flashcardLinks.stream()
        .filter(link -> link.getFlashcard().equals(flashcard))
        .findFirst();
  }

  public void removeFlashcardLink(DeckFlashcard deckFlashcard) {
    flashcardLinks.remove(deckFlashcard);
  }

  public int size() {
    return flashcardLinks.size();
  }
}
