package com.antunes.flashcards.domain.user.auth.repository;

import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import com.antunes.flashcards.domain.user.model.User;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);

  Optional<PasswordResetToken> findByUser(User user);

  @Modifying
  @Transactional
  @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt <= :now OR t.used = true")
  void deleteAllExpiredOrUsed(@Param("now") Instant now);
}
