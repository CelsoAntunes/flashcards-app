package com.antunes.flashcards.domain.deck.repository;

import com.antunes.flashcards.domain.deck.model.Deck;
import com.antunes.flashcards.domain.user.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
  @EntityGraph(attributePaths = "owner")
  List<Deck> findByOwner(User owner);
}
