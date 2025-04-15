package com.antunes.flashcards.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.exception.EmailValidationException;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {

  @Mock private UserRepository userRepository;

  @Mock private PasswordFactory passwordFactory;

  @InjectMocks private UserService userService;

  @Test
  void shouldRegisterUserWithValidCredentials() {
    String rawEmail = "user@example.com";
    String rawPassword = "securePassword123";
    String hashedPassword = "hashed-password";

    Password mockedPassword = mock(Password.class);
    when(mockedPassword.getHashedPassword()).thenReturn(hashedPassword);
    when(passwordFactory.create(rawPassword)).thenReturn(mockedPassword);

    User mockedUser = new User(rawEmail, mockedPassword);
    when(userRepository.save(any(User.class))).thenReturn(mockedUser);

    User user = userService.register(rawEmail, rawPassword);

    assertNotNull(user.getId());
    assertEquals(rawEmail, user.getEmail());

    verify(userRepository, times(1)).save(any(User.class));
    verify(passwordFactory, times(1)).create(rawPassword);
    verify(userRepository).save(argThat(savedUser -> savedUser.getEmail().equals(rawEmail)));
  }

  @Test
  void shouldNotRegisterUserWithExistingEmail() {
    String rawEmail = "user@example.com";
    String rawPassword = "securePassword123";

    when(userRepository.existsByEmail(rawEmail)).thenReturn(true);

    ExistingEmailException exception =
        assertThrows(
            ExistingEmailException.class, () -> userService.register(rawEmail, rawPassword));
    assertEquals("Email already exists", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForInvalidEmail() {
    String invalidEmail = "not-an-email";
    String password = "securePassword123";

    EmailValidationException exception =
        assertThrows(
            EmailValidationException.class, () -> userService.register(invalidEmail, password));
    assertEquals("Not a valid email", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForNullEmail() {
    String password = "securePassword123";

    EmailValidationException exception =
        assertThrows(EmailValidationException.class, () -> userService.register(null, password));
    assertEquals("Email cannot be null", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForShortPassword() {
    String rawEmail = "user@example.com";
    String shortPassword = "short";
    PasswordValidationException exception =
        assertThrows(
            PasswordValidationException.class, () -> userService.register(rawEmail, shortPassword));
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }
}
