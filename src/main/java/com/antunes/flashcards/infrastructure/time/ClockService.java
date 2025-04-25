package com.antunes.flashcards.infrastructure.time;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class ClockService {
  private Clock clock;

  public ClockService(Clock clock) {
    this.clock = clock;
  }

  public Clock getClock() {
    return clock;
  }

  // For testing only
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  public LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  public boolean isAfter(LocalDateTime dateTime, LocalDateTime other) {
    return dateTime.isAfter(other);
  }

  public boolean isBefore(LocalDateTime dateTime, LocalDateTime other) {
    return dateTime.isBefore(other);
  }

  public LocalDateTime minusMinutes(LocalDateTime dateTime, long minutes) {
    return dateTime.minusMinutes(minutes);
  }

  public LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
    return dateTime.plusMinutes(minutes);
  }
}
