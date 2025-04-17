package com.antunes.flashcards.domain.flashcard.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flascard.model.Flashcard;
import com.antunes.flashcards.domain.flascard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class FlashcardRepositoryUnitTests {

  @Autowired private FlashcardRepository flashcardRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordFactory passwordFactory;

  @Test
  void shouldSaveFlashcardWithOwner() {
    Email email = new Email("user@email.com");
    Password password = passwordFactory.create("securePassword123");
    User user = new User(email, password);
    userRepository.save(user);
    Flashcard flashcard = new Flashcard("question", "answer", user);
    Flashcard savedFlashcard = flashcardRepository.save(flashcard);
    assertNotNull(savedFlashcard.getId());
    assertEquals("question", flashcard.getQuestion());
    assertEquals("answer", flashcard.getAnswer());
    assertEquals(user, flashcard.getOwner());
  }
}
