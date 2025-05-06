package com.antunes.flashcards.domain.deck.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.deck.exception.DeckValidationException;
import com.antunes.flashcards.domain.deck.exception.ExistingFlashcardException;
import com.antunes.flashcards.domain.deck.repository.DeckFlashcardRepository;
import com.antunes.flashcards.domain.deck.repository.DeckRepository;
import com.antunes.flashcards.domain.flashcard.exception.FlashcardNotFoundException;
import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.flashcard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.flashcard.service.FlashcardService;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
public class DeckTests {
  @Autowired FlashcardRepository flashcardRepository;
  @Autowired UserRepository userRepository;
  @Autowired DeckRepository deckRepository;
  @Autowired DeckFlashcardRepository deckFlashcardRepository;
  @Autowired UserService userService;
  @Autowired FlashcardService flashcardService;
  @Autowired EntityManager entityManager;

  private String rawEmail = "user@example.com";
  private String rawPassword = "securePassword123";
  private User user;
  private Deck deck;
  private Flashcard flashcard;
  private Flashcard anotherFlashcard;

  @BeforeAll
  void setUp() {
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userRepository.flush();
    user = userService.register(rawEmail, rawPassword);
    userRepository.flush();
    flashcard = flashcardService.createFlashcard("question", "answer", user);
    anotherFlashcard = flashcardService.createFlashcard("another question", "another answer", user);
  }

  @BeforeEach
  void cleanup() {
    deckFlashcardRepository.deleteAll();
    deckRepository.deleteAll();
    deck = Deck.create("Deck", user);
    deckRepository.flush();
  }

  @Test
  void createdDeckShouldNotHaveAnyLinksWithCorrectTitleAndOwner() {
    assertEquals(0, deck.size());
    assertEquals("Deck", deck.getTitle());
    assertEquals(user, deck.getOwner());
  }

  @Test
  void shouldAddDeckFlashcardLinkToDeck() {
    deck.addFlashcard(flashcard);
    assertEquals(1, deck.size());
  }

  @Test
  void findLinkWithFlashcardShouldRetrieveAddedFlashcardLink() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertEquals(
        deck.findLinkWithFlashcard(flashcard).get(),
        deckFlashcardRepository.findByDeck(deck).get(0));
  }

  @Test
  void hasFlashcardShouldReturnTrueWithAddedFlashcardLink() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertTrue(deck.hasFlashcard(flashcard));
  }

  @Test
  void hasFlashcardShouldReturnFalseWithFlashcardLinkNotAdded() {
    assertFalse(deck.hasFlashcard(flashcard));
  }

  @Test
  void removeFlashcardShouldRemoveExistingFlashcardLink() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertEquals(1, deck.size());
    DeckFlashcard deckFlashcard = deck.findLinkWithFlashcard(flashcard).get();
    deck.removeFlashcardLink(deckFlashcard);
    deckRepository.save(deck);
    assertEquals(0, deck.size());
    assertFalse(deck.hasFlashcard(flashcard));
  }

  @Test
  void sizeShouldTrackAddedFlashcardLinks() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertEquals(1, deck.size());
    deck.addFlashcard(anotherFlashcard);
    deckRepository.save(deck);
    assertEquals(2, deck.size());
    assertTrue(deck.hasFlashcard(flashcard));
    assertTrue(deck.hasFlashcard(anotherFlashcard));
  }

  @Test
  void addFlashcardTwiceShouldThrow() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    ExistingFlashcardException exception =
        assertThrows(ExistingFlashcardException.class, () -> deck.addFlashcard(flashcard));
    assertEquals("This flashcard already exists in this deck", exception.getMessage());
  }

  @Test
  void removeFlashcardFromEmptyDeckShouldThrow() {
    Deck anotherDeck = Deck.create("Not the deck", user);
    DeckFlashcard deckFlashcard = DeckFlashcard.link(anotherDeck, flashcard);
    FlashcardNotFoundException exception =
        assertThrows(
            FlashcardNotFoundException.class, () -> deck.removeFlashcardLink(deckFlashcard));
    assertEquals("Flashcard is not in the deck", exception.getMessage());
  }

  @Test
  void titleOfADeckCannotBeNull() {
    DeckValidationException exception =
        assertThrows(DeckValidationException.class, () -> Deck.create(null, user));
    assertEquals("Title cannot be null or blank", exception.getMessage());
  }

  @Test
  void titleOfADeckCannotBeBlank() {
    DeckValidationException exception =
        assertThrows(DeckValidationException.class, () -> Deck.create("  ", user));
    assertEquals("Title cannot be null or blank", exception.getMessage());
  }

  @Test
  void userCannotBeNull() {
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> Deck.create("title", null));
    assertEquals("User does not exist", exception.getMessage());
  }

  @Test
  @Transactional
  void addAndRemoveAndAddShouldNotThrow() {
    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertEquals(1, deck.size());

    DeckFlashcard deckFlashcard = deck.findLinkWithFlashcard(flashcard).get();
    deck.removeFlashcardLink(deckFlashcard);
    deckRepository.save(deck);
    assertEquals(0, deck.size());

    deck.addFlashcard(flashcard);
    deckRepository.save(deck);
    assertEquals(1, deck.size());
    assertTrue(deck.hasFlashcard(flashcard));
  }
}
