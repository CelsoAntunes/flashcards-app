package com.antunes.flashcards.domain.user.validation;

import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
  Integer minDigits = 8;

  public void assertValid(String password) {
    if (password == null) throw new PasswordValidationException("Password cannot be null");
    if (password.isBlank()) throw new PasswordValidationException("Password cannot be blank");
    if (password.length() < minDigits)
      throw new PasswordValidationException("Password must be at least 8 characters long");
    if (password.chars().noneMatch(Character::isUpperCase))
      throw new PasswordValidationException("Password must contain an uppercase letter");
    if (password.chars().noneMatch(Character::isLowerCase))
      throw new PasswordValidationException("Password must contain a lowercase letter");
    if (password.chars().noneMatch(Character::isDigit))
      throw new PasswordValidationException("Password must contain a number");
  }
}
