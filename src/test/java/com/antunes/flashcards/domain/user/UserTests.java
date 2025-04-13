package com.antunes.flashcards.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import org.junit.jupiter.api.Test;

public class UserTests {
  PasswordFactory passwordFactory =
      new PasswordFactory(new PasswordValidator(), new StubPasswordEncoder());

  @Test
  void shouldCreateUserWithValidEmailAndPassword() {
    String email = "user@example.com";
    String password = "secretPassword123";

    User user = new User(email, password, passwordFactory);

    assertEquals(email, user.getEmail());
    assertNotNull(user.getHashedPassword());
    assertNotEquals(password, user.getHashedPassword());
    assertTrue(user.getHashedPassword().startsWith("$2"), "Password should be bcrypt hash");
  }
}
