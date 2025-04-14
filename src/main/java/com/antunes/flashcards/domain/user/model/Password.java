package com.antunes.flashcards.domain.user.model;

import jakarta.persistence.Embeddable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
public class Password {

  private final String hashedPassword;

  public Password(String rawPassword, PasswordEncoder passwordEncoder) {
    this.hashedPassword = passwordEncoder.encode(rawPassword);
  }

  public boolean matches(String plainTextPassword, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(plainTextPassword, this.hashedPassword);
  }

  public String getHashedPassword() {
    return this.hashedPassword;
  }
}
