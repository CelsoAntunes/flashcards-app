package com.antunes.flashcards.domain.flashcard.repository;

import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.user.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
  @EntityGraph(attributePaths = "owner")
  List<Flashcard> findByOwner(User owner);
}
