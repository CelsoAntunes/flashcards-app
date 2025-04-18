package com.antunes.flashcards.domain.user.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.StubPasswordEncoder;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationUnitTests {
  private final String rawEmail = "user@example.com";
  private final String rawPassword = "securePassword123";

  private LoginService loginService;

  @Mock UserRepository userRepository;
  @Mock PasswordFactory passwordFactory;
  private final PasswordEncoder passwordEncoder = new StubPasswordEncoder();

  @BeforeEach
  void setUp() {
    loginService = new LoginService(userRepository, passwordFactory, passwordEncoder);
  }

  @Test
  void registeredUserCanLoginCorrectPassword() {
    Email email = new Email(rawEmail);
    Password mockedPassword = mock(Password.class);
    when(passwordFactory.create(rawPassword)).thenReturn(mockedPassword);
    User mockedUser = new User(email, mockedPassword);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));
    when(passwordEncoder.matches(rawPassword, "$2stub$" + rawPassword)).thenReturn(true);
    String token = loginService.login(rawEmail, rawPassword);
    assertNotNull(token);
  }
}
