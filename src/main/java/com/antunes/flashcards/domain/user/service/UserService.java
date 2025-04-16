package com.antunes.flashcards.domain.user.service;

import com.antunes.flashcards.domain.user.PasswordFactory;
import com.antunes.flashcards.domain.user.exception.ExistingEmailException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.Password;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import com.antunes.flashcards.domain.user.validation.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordFactory passwordFactory;
  private final PasswordValidator passwordValidator;

  @Autowired
  public UserService(
      UserRepository userRepository,
      PasswordFactory passwordFactory,
      PasswordValidator passwordValidator) {
    this.userRepository = userRepository;
    this.passwordFactory = passwordFactory;
    this.passwordValidator = passwordValidator;
  }

  public boolean emailExists(Email email) {
    return userRepository.findByEmail(email).isPresent();
  }

  public User register(String rawEmail, String rawPassword) {
    Email email = new Email(rawEmail);
    if (emailExists(email)) {
      throw new ExistingEmailException("Email already exists");
    }
    passwordValidator.assertValid(rawPassword);
    Password password = passwordFactory.create(rawPassword);
    User user = new User(email, password);
    return userRepository.save(user);
  }
}
