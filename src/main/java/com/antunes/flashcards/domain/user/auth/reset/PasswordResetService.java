package com.antunes.flashcards.domain.user.auth.reset;

import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetService {
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public PasswordResetService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public String reset(String rawEmail) {
    Email email = new Email(rawEmail);
    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
      throw new UserNotFoundException("No accounts with this email");
    }
    User user = userOptional.get();
    return jwtTokenProvider.generateResetToken(user.getEmail(), user.getId());
  }
}
