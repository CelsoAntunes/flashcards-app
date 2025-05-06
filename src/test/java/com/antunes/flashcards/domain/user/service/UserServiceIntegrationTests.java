package com.antunes.flashcards.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.flashcard.repository.FlashcardRepository;
import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import com.antunes.flashcards.domain.user.exception.ExistingEmailException;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTests {
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @Autowired private FlashcardRepository flashcardRepository;

  @BeforeEach
  public void setUp() {
    flashcardRepository.deleteAll();
    userRepository.deleteAll();
  }

  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  @Test
  void createValidUser() {
    User user = userService.register(rawEmail, rawPassword);
    assertNotNull(user.getId());
    assertEquals(rawEmail, user.getEmail());
    assertTrue(user.getHashedPassword().startsWith("$2"), "Password should be a bcrypt hash");
  }

  @Test
  void createUserWithExistingEmail_shouldThrow() {
    userService.register(rawEmail, rawPassword);

    ExistingEmailException exception =
        assertThrows(
            ExistingEmailException.class, () -> userService.register(rawEmail, rawPassword));
    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void createUserWithExistingEmailUppercase_shouldThrow() {
    userService.register(rawEmail, rawPassword);

    ExistingEmailException exception =
        assertThrows(
            ExistingEmailException.class,
            () -> userService.register(rawEmail.toUpperCase(), rawPassword));
    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void emailIsNormalizedOnRegister() {
    String inputEmail = "User@Example.Com";
    User user = userService.register(inputEmail, rawPassword);
    assertEquals("user@example.com", user.getEmail());
  }

  @Test
  void invalidEmailShouldThrow() {
    String invalidEmail = "not-an-email";
    EmailValidationException exception =
        assertThrows(
            EmailValidationException.class, () -> userService.register(invalidEmail, rawPassword));
    assertEquals("Not a valid email", exception.getMessage());
  }

  @Test
  void shortPasswordShouldThrow() {
    String shortPassword = "short";
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class, () -> userService.register(rawEmail, shortPassword));
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }
}
