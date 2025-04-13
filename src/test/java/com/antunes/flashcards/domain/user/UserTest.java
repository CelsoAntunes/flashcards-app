package com.antunes.flashcards.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {
  @Test
  void shouldCreateUserWithValidEmailAndPassword() {
    Email email = new Email("user@example.com");
    Password password = new Password("secretPassword123");
    User user = new User(email, password);

    assertEquals(email, user.getEmail());
    assertEquals(password, user.getPassword());
  }
}
