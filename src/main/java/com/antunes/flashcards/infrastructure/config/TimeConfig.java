package com.antunes.flashcards.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class TimeConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
