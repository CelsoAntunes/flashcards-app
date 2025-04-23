package com.antunes.flashcards.domain.user.auth.repository;

import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);

  Optional<PasswordResetToken> findByUser(User user);
}
