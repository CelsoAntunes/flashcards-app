package com.antunes.flashcards.domain.user;

import org.springframework.security.crypto.password.PasswordEncoder;

public class StubPasswordEncoder implements PasswordEncoder {
  @Override
  public String encode(CharSequence rawPassword) {
    return "$2stub$" + rawPassword;
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return encodedPassword.equals("$2stub$" + rawPassword);
  }
}
