package com.antunes.flashcards.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfig {
  public SchedulingConfig() {
    System.out.println("Scheduling enabled outside the test profile");
  }
}
