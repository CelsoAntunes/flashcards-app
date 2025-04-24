package com.antunes.flashcards.domain.user.auth.model;

import com.antunes.flashcards.domain.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

@Table(
    name = "password_reset_token",
    indexes = {
      @Index(name = "idx_token", columnList = "token"),
      @Index(name = "idx_expiresAt", columnList = "expiresAt"),
      @Index(name = "idx_used", columnList = "used")
    })
@Entity
public class PasswordResetToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean used = false;

  public PasswordResetToken() {}

  public PasswordResetToken(User user, String token, Instant expiresAt) {
    this.user = user;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public Long getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public User getUser() {
    return user;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isUsed() {
    return used;
  }

  public void markAsUsed() {
    this.used = true;
  }

  public boolean isUsable() {
    return !this.used && !this.isExpired();
  }
}
