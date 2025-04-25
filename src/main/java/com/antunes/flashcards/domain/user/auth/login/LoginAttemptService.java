package com.antunes.flashcards.domain.user.auth.login;

import com.antunes.flashcards.domain.user.auth.model.LoginAttempt;
import com.antunes.flashcards.domain.user.auth.repository.LoginAttemptRepository;
import com.antunes.flashcards.domain.user.model.User;
import com.antunes.flashcards.infrastructure.time.ClockService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
  @Autowired private final LoginAttemptRepository loginAttemptRepository;
  @Autowired private final ClockService clockService;

  public LoginAttemptService(
      LoginAttemptRepository loginAttemptRepository, ClockService clockService) {
    this.loginAttemptRepository = loginAttemptRepository;
    this.clockService = clockService;
  }

  public LoginAttempt getOrCreate(User user) {
    return loginAttemptRepository
        .findByUser(user)
        .orElseGet(() -> loginAttemptRepository.save(new LoginAttempt(user, clockService)));
  }

  @Transactional
  public void registerFailedAttempt(User user) {
    LoginAttempt loginAttempt = getOrCreate(user);
    loginAttempt.incrementAttempts(clockService);
  }

  public int getAttemptCount(User user) {
    LoginAttempt loginAttempt = getOrCreate(user);
    return loginAttempt.getAttemptCount();
  }

  @Transactional
  public void onSuccessfulLogin(User user) {
    LoginAttempt loginAttempt = getOrCreate(user);
    loginAttempt.resetAttempts();
    loginAttemptRepository.save(loginAttempt);
  }

  public boolean isLocked(User user) {
    LoginAttempt loginAttempt = getOrCreate(user);
    return loginAttempt.isLocked();
  }

  @Transactional
  public void unlockIfEligible(User user) {
    LoginAttempt loginAttempt = getOrCreate(user);
    if (loginAttempt.shouldUnlock(clockService)) {
      loginAttempt.unlock();
      loginAttemptRepository.saveAndFlush(loginAttempt);
    }
  }
}
