package com.antunes.flashcards.infrastructure.time;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestTimeConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
