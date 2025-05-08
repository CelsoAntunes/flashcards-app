package com.antunes.flashcards.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.PasswordValidator;
import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import com.antunes.flashcards.domain.user.exception.ExistingEmailException;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {

  @Mock private UserRepository userRepository;
  @Mock private PasswordFactory passwordFactory;
  private final PasswordValidator passwordValidator = new PasswordValidator();

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, passwordFactory, passwordValidator);
  }

  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  @Test
  void shouldRegisterUserWithValidCredentials() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(passwordFactory.create(rawPassword)).thenReturn(mockedPassword);
    User mockedUser = new User(email, mockedPassword);
    when(userRepository.save(any(User.class))).thenReturn(mockedUser);

    User user = userService.register(rawEmail, rawPassword);

    assertNotNull(user.getEmail());
    assertEquals(rawEmail, user.getEmail().getValue());

    verify(userRepository, times(1)).save(any(User.class));
    verify(passwordFactory, times(1)).create(rawPassword);
    verify(userRepository)
        .save(argThat(savedUser -> savedUser.getEmail().getValue().equals(rawEmail)));
  }

  @Test
  void shouldNotRegisterUserWithExistingEmail() {
    Email email = new Email(rawEmail);
    User existingUser = new User(email, mock(Password.class));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    ExistingEmailException exception =
        assertThrows(
            ExistingEmailException.class, () -> userService.register(rawEmail, rawPassword));
    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForInvalidEmail() {
    String invalidEmail = "not-an-email";

    EmailValidationException exception =
        assertThrows(
            EmailValidationException.class, () -> userService.register(invalidEmail, rawPassword));
    assertEquals("Not a valid email", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForNullEmail() {
    EmailValidationException exception =
        assertThrows(EmailValidationException.class, () -> userService.register(null, rawPassword));
    assertEquals("Email cannot be null", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForShortPassword() {
    String shortPassword = "short";
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class, () -> userService.register(rawEmail, shortPassword));
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }
}
