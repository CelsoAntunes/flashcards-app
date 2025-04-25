package com.antunes.flashcards.domain.user.auth.model;

import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.infrastructure.time.ClockService;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
public class LoginAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Getter
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Getter private int attemptCount;

  private LocalDateTime lastAttemptTime;

  private LocalDateTime lockedUntil;

  private LoginAttempt() {}

  public LoginAttempt(User user, ClockService clockService) {
    this.user = user;
    this.attemptCount = 0;
    this.lastAttemptTime = clockService.now();
  }

  public void incrementAttempts(ClockService clockService) {
    this.attemptCount++;
    this.lastAttemptTime = clockService.now();
    if (attemptCount >= 5) {
      lockedUntil = clockService.plusMinutes(lastAttemptTime, 15);
    }
  }

  public boolean isLocked() {
    return lockedUntil != null;
  }

  public boolean shouldUnlock(ClockService clockService) {
    return lockedUntil != null && clockService.now().isAfter(lockedUntil);
  }

  public void resetAttempts() {
    this.attemptCount = 0;
  }

  public void unlock() {
    resetAttempts();
    this.lockedUntil = null;
  }
}
