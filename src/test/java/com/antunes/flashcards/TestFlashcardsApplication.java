package com.antunes.flashcards;

import org.springframework.boot.SpringApplication;

public class TestFlashcardsApplication {

  public static void main(String[] args) {
    SpringApplication.from(FlashcardsApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
