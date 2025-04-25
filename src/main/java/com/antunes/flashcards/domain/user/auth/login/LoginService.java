package com.antunes.flashcards.domain.user.auth.login;

import com.antunes.flashcards.domain.user.auth.token.JwtTokenProvider;
import com.antunes.flashcards.domain.user.exception.PasswordValidationException;
import com.antunes.flashcards.domain.user.exception.UserNotFoundException;
import com.antunes.flashcards.domain.user.exception.UserTimeoutException;
import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.domain.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final LoginAttemptService loginAttemptService;

  @Autowired
  public LoginService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider,
      LoginAttemptService loginAttemptService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.loginAttemptService = loginAttemptService;
  }

  public String login(String rawEmail, String rawPassword) {
    Email email = new Email(rawEmail);
    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
      throw new UserNotFoundException("No accounts with this email");
    }
    User user = userOptional.get();
    loginAttemptService.unlockIfEligible(user);
    if (loginAttemptService.isLocked(user)) {
      throw new UserTimeoutException("Your account was timedout for 15 minutes");
    }
    String storedHashed = user.getHashedPassword();
    if (!passwordEncoder.matches(rawPassword, storedHashed)) {
      loginAttemptService.registerFailedAttempt(user);
      throw new PasswordValidationException("Incorrect password");
    }
    loginAttemptService.onSuccessfulLogin(user);
    return jwtTokenProvider.generateAuthToken(user.getEmail(), user.getId());
  }
}
