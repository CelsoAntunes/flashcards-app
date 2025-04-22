package com.antunes.flashcards.domain.user.auth;

import com.antunes.flashcards.domain.user.model.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordFactory {
  private final PasswordValidator passwordValidator;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public PasswordFactory(PasswordValidator passwordValidator, PasswordEncoder passwordEncoder) {
    this.passwordValidator = passwordValidator;
    this.passwordEncoder = passwordEncoder;
  }

  public Password create(String rawPassword) {
    passwordValidator.assertValid(rawPassword);
    return new Password(rawPassword, passwordEncoder);
  }
}
