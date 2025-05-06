package com.antunes.flashcards.domain.deck.repository;

import com.antunes.flashcards.domain.deck.model.Deck;
import com.antunes.flashcards.domain.deck.model.DeckFlashcard;
import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckFlashcardRepository extends JpaRepository<DeckFlashcard, Long> {
  @EntityGraph(attributePaths = "deck")
  List<DeckFlashcard> findByDeck(Deck deck);

  @EntityGraph(attributePaths = "flashcard")
  List<DeckFlashcard> findByFlashcard(Flashcard flashcard);
}
