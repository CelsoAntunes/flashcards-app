package com.antunes.flashcards.infrastructure.maintenance;

import com.antunes.flashcards.domain.user.auth.repository.PasswordResetTokenRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenCleanupService {

  private static final Logger logger =
      LoggerFactory.getLogger(PasswordResetTokenCleanupService.class);

  private final PasswordResetTokenRepository passwordResetTokenRepository;

  public PasswordResetTokenCleanupService(
      PasswordResetTokenRepository passwordResetTokenRepository) {
    this.passwordResetTokenRepository = passwordResetTokenRepository;
  }

  @Scheduled(fixedRate = 300000)
  public void cleanupExpiredOrUsedTokens() {
    Instant now = Instant.now();
    passwordResetTokenRepository.deleteAllExpiredOrUsed(now);
    logger.info("Expired or used password reset tokens cleanup executed at {}", now);
  }
}
