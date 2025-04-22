package com.antunes.flashcards.domain.user.auth.reset;

import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetService {
  private final UserRepository userRepository;
  private final PasswordFactory passwordFactory;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public PasswordResetService(
      UserRepository userRepository,
      PasswordFactory passwordFactory,
      JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.passwordFactory = passwordFactory;
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

  @Transactional
  public void resetPassword(String token, String newPassword) {
    jwtTokenProvider.validateToken(token, TokenType.RESET);
    Claims claims = jwtTokenProvider.parseToken(token);
    String rawEmail = claims.getSubject();
    Email email = new Email(rawEmail);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UserNotFoundException("User not found for the provided reset token"));
    Password newPasswordHashed = passwordFactory.create(newPassword);
    User updatedUser = User.withUpdatedPassword(user, newPasswordHashed);
    userRepository.save(updatedUser);
  }
}
