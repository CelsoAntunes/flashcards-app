package com.antunes.flashcards.domain.deck.service;

import com.antunes.flashcards.domain.deck.model.Deck;
import com.antunes.flashcards.domain.deck.model.DeckFlashcard;
import com.antunes.flashcards.domain.deck.repository.DeckFlashcardRepository;
import com.antunes.flashcards.domain.deck.repository.DeckRepository;
import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DeckService {

  @Autowired DeckRepository deckRepository;
  @Autowired FlashcardRepository flashcardRepository;
  @Autowired DeckFlashcardRepository deckFlashcardRepository;

  public DeckService(
      DeckRepository deckRepository,
      FlashcardRepository flashcardRepository,
      DeckFlashcardRepository deckFlashcardRepository) {
    this.deckRepository = deckRepository;
    this.flashcardRepository = flashcardRepository;
    this.deckFlashcardRepository = deckFlashcardRepository;
  }

  public void addFlashcardToDeck(Long deckId, Long flashcardId) {
    Deck deck =
        deckRepository
            .findById(deckId)
            .orElseThrow(() -> new EntityNotFoundException("Deck not found"));
    Flashcard flashcard =
        flashcardRepository
            .findById(flashcardId)
            .orElseThrow(() -> new EntityNotFoundException("Flashcard not found"));

    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
  }

  public void removeFlashcardFromDeck(Long deckId, Long flashcardId) {
    Deck deck =
        deckRepository
            .findById(deckId)
            .orElseThrow(() -> new EntityNotFoundException("Deck not found"));

    DeckFlashcard link =
        deck.findLinkWithFlashcard(flashcardId)
            .orElseThrow(() -> new EntityNotFoundException("Flashcard not linked to deck"));

    deck.removeFlashcardLink(link);
    deckFlashcardRepository.delete(link);
  }
}
