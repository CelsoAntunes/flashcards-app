package com.antunes.flashcards.domain.user.auth.login;

import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public LoginService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public String login(String rawEmail, String rawPassword) {
    Email email = new Email(rawEmail);
    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
      throw new UserNotFoundException("No accounts with this email");
    }
    User user = userOptional.get();
    String storedHashed = user.getHashedPassword();
    if (!passwordEncoder.matches(rawPassword, storedHashed)) {
      throw new PasswordValidationException("Incorrect password");
    }
    return jwtTokenProvider.generateAuthToken(user.getEmail(), user.getId());
  }
}
