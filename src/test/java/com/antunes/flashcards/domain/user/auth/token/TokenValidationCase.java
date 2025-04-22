package com.antunes.flashcards.domain.user.auth.token;

public record TokenValidationCase(
    String name,
    TokenScenario scenario,
    String token,
    Class<? extends Exception> expectedException,
    String expectedMessage) {
  public boolean expectsException() {
    return expectedException != null;
  }
}
