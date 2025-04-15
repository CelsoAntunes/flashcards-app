package com.antunes.flashcards.domain.user.model;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import org.junit.jupiter.api.Test;

public class UserTests {
  PasswordFactory passwordFactory =
      new PasswordFactory(new PasswordValidator(), new StubPasswordEncoder());

  @Test
  void shouldCreateUserWithValidEmailAndPassword() {
    String email = "user@example.com";
    String rawPassword = "secretPassword123";

    Password password = passwordFactory.create(rawPassword);

    User user = new User(email, password);

    assertEquals(email, user.getEmail());
    assertNotNull(user.getHashedPassword());
    assertNotEquals(password, user.getHashedPassword());
    assertTrue(user.getHashedPassword().startsWith("$2stub$"), "Password should be bcrypt hash");
  }
}
