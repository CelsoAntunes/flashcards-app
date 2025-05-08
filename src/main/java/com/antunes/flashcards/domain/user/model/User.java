package com.antunes.flashcards.domain.user.model;

import com.antunes.flashcards.domain.deck.model.Deck;
import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import com.antunes.flashcards.domain.user.auth.model.LoginAttempt;
import com.antunes.flashcards.domain.user.auth.model.PasswordResetToken;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Entity
@Table(name = "users")
public class User {
  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter @Column private String username;

  @Embedded @Getter private Email email;

  @Embedded private Password password;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Flashcard> flashcards;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Deck> decks;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PasswordResetToken> passwordResetTokens;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private LoginAttempt loginAttempt;

  protected User() {}

  public User(Email email, Password password) {
    this.username = email.getUsername();
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

  public void changeName(String newUsername) {
    if (newUsername == null || newUsername.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    this.username = newUsername.trim();
  }

  public String getHashedPassword() {
    return password.getHashedPassword();
  }

  public static User withUpdatedPassword(User original, Password newPasswordHash) {
    User updated = new User();
    updated.id = original.id;
    updated.username = original.username;
    updated.email = original.email;
    updated.password = newPasswordHash;
    return updated;
  }
}
