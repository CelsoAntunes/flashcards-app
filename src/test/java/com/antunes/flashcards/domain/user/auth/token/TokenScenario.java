package com.antunes.flashcards.domain.user.auth.token;

public enum TokenScenario {
  AUTH(TokenType.AUTH) {
    @Override
    public String generateToken(JwtTokenProvider provider, String email, Long userId) {
      return provider.generateAuthToken(email, userId);
    }
  },
  RESET(TokenType.RESET) {
    @Override
    public String generateToken(JwtTokenProvider provider, String email, Long userId) {
      return provider.generateResetToken(email, userId);
    }
  };

  public final TokenType tokenType;

  TokenScenario(TokenType tokenType) {
    this.tokenType = tokenType;
  }

  public TokenType tokenType() {
    return tokenType;
  }

  public abstract String generateToken(JwtTokenProvider provider, String email, Long userId);
}
