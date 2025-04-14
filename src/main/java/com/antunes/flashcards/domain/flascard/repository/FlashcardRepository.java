package com.antunes.flashcards.domain.flascard.repository;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {}
