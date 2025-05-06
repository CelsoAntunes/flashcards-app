package com.antunes.flashcards.domain.flashcard.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.PasswordValidator;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@Import(FlashcardRepositoryTests.TestConfig.class)
public class FlashcardRepositoryTests {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public PasswordFactory passwordFactory() {
      PasswordValidator validator = new PasswordValidator();
      PasswordEncoder encoder = new BCryptPasswordEncoder();
      return new PasswordFactory(validator, encoder);
    }
  }

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
    assertEquals("question", savedFlashcard.getQuestion());
    assertEquals("answer", savedFlashcard.getAnswer());
    assertEquals(user, savedFlashcard.getOwner());
  }
}
