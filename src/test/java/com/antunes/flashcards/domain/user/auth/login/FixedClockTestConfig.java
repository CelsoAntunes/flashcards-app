package com.antunes.flashcards.domain.user.auth.login;

import com.antunes.flashcards.infrastructure.time.ClockService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FixedClockTestConfig {
  @Bean
  public Clock clock() {
    return Clock.fixed(Instant.now(), ZoneId.systemDefault());
  }

  @Bean
  public ClockService clockService(Clock clock) {
    return new ClockService(clock);
  }

  public void setTime(ClockService clockService, Duration duration) {
    Clock currentClock = clockService.getClock();
    Instant newInstant = currentClock.instant().plus(duration);
    Clock newClock = Clock.fixed(newInstant, currentClock.getZone());
    clockService.setClock(newClock);
  }
}
