package com.antunes.flashcards.domain.user.auth.reset;

import com.antunes.flashcards.domain.user.auth.PasswordFactory;
import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.auth.token.TokenType;
import com.antunes.flashcards.domain.user.exception.ResetTokenNotFoundException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetService {
  private final UserRepository userRepository;
  private final PasswordFactory passwordFactory;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordResetTokenRepository passwordResetTokenRepository;

  @PersistenceContext EntityManager entityManager;

  @Autowired
  public PasswordResetService(
      UserRepository userRepository,
      PasswordFactory passwordFactory,
      JwtTokenProvider jwtTokenProvider,
      PasswordResetTokenRepository passwordResetTokenRepository) {
    this.userRepository = userRepository;
    this.passwordFactory = passwordFactory;
    this.jwtTokenProvider = jwtTokenProvider;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
  }

  @Transactional
  public String reset(String rawEmail) {
    Email email = new Email(rawEmail);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No accounts with this email"));
    String token = jwtTokenProvider.generateResetToken(user.getEmail().getValue(), user.getId());
    PasswordResetToken resetToken =
        new PasswordResetToken(
            user, token, jwtTokenProvider.parseToken(token).getExpiration().toInstant());
    passwordResetTokenRepository.save(resetToken);
    return token;
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new ResetTokenNotFoundException("Reset token not found"));
    if (!resetToken.isUsable()) {
      throw new TokenExpiredException("Token is either expired or already used");
    }
    jwtTokenProvider.validateToken(token, TokenType.RESET);
    Email email = new Email(jwtTokenProvider.parseToken(token).getSubject());
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UserNotFoundException("User not found for the provided reset token"));

    Password newPasswordHashed = passwordFactory.create(newPassword);
    User updatedUser = User.withUpdatedPassword(user, newPasswordHashed);
    entityManager.merge(updatedUser);

    resetToken.markAsUsed();
    passwordResetTokenRepository.save(resetToken);

    entityManager.flush();
    entityManager.clear();
  }
}
