package com.antunes.flashcards.domain.deck.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class DeckTests {
  @Autowired FlashcardRepository flashcardRepository;
  @Autowired UserRepository userRepository;
  @Autowired UserService userService;

  private String rawEmail = "user@example.com";
  private String rawPassword = "securePassword123";
  private User user;

  @BeforeAll
  void setUp() {
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
    userRepository.flush();
    user = userService.register(rawEmail, rawPassword);
    userRepository.flush();
  }

  @Test
  void shouldCreateDeckWithTitleAndOwner() {
    Deck deck = Deck.create("Deck", user);
    assertEquals("Deck", deck.getTitle());
    assertEquals(user, deck.getOwner());
  }
}
