package com.antunes.flashcards.domain.user.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded private Email email;

  @Embedded private Password password;

  protected User() {
    this.email = null;
    this.password = null;
  }

  public User(Email email, Password password) {
    this.email = email;
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email.getValue();
  }

  public String getHashedPassword() {
    return password.getHashedPassword();
  }

  public static User withUpdatedPassword(User original, Password newPasswordHash) {
    User updated = new User();
    updated.id = original.id;
    updated.email = original.email;
    updated.password = newPasswordHash;
    return updated;
  }
}
