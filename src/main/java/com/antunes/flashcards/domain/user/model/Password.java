package com.antunes.flashcards.domain.user.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
public class Password {

  private String hashedPassword;

  protected Password() {}

  public Password(String rawPassword, PasswordEncoder passwordEncoder) {
    this.hashedPassword = passwordEncoder.encode(rawPassword);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    Password password = (Password) o;
    return Objects.equals(hashedPassword, password.getHashedPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashedPassword);
  }

  @Override
  public String toString() {
    return "Password{hidden}";
  }

  public boolean matches(String plainTextPassword, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(plainTextPassword, this.hashedPassword);
  }

  public String getHashedPassword() {
    return this.hashedPassword;
  }
}
