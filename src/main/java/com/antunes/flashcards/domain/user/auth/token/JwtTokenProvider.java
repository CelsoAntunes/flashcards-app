package com.antunes.flashcards.domain.user.auth.token;

import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import com.antunes.flashcards.domain.user.exception.TokenValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
  @Value("${spring.jwt.secret}")
  private String secret;

  private SecretKey secretKey;

  private final Duration AUTH_TOKEN_DURATION = Duration.ofHours(1);
  private final Duration RESET_TOKEN_DURATION = Duration.ofMinutes(15);

  @PostConstruct
  public void init() {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public void setSecretKey(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  public SecretKey getSecretKey() {
    return this.secretKey;
  }

  private Instant buildExpiration(Duration duration) {
    return Instant.now().plus(duration);
  }

  public String generateAuthToken(String subject, Long userId) {
    return Jwts.builder()
        .subject(subject)
        .claim("userId", userId)
        .claim("type", TokenType.AUTH.name())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(buildExpiration(AUTH_TOKEN_DURATION)))
        .signWith(secretKey)
        .compact();
  }

  public String generateResetToken(String subject, Long userId) {
    return Jwts.builder()
        .subject(subject)
        .claim("userId", userId)
        .claim("type", TokenType.RESET.name())
        .claim("jti", UUID.randomUUID().toString())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(buildExpiration(RESET_TOKEN_DURATION)))
        .signWith(secretKey)
        .compact();
  }

  public void validateToken(String token, TokenType expectedType) {
    if (token == null || token.isBlank()) {
      throw new TokenValidationException("Token cannot be null or blank");
    }
    try {
      Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

      Date expiration = claimsJws.getPayload().getExpiration();
      if (expiration != null && expiration.before(new Date())) {
        throw new TokenExpiredException("Token has expired");
      }

      String type = claimsJws.getPayload().get("type", String.class);
      if (!expectedType.name().equals(type)) {
        throw new TokenValidationException("Unexpected token type");
      }
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Token has expired");
    } catch (JwtException e) {
      throw new TokenValidationException("Invalid token");
    }
  }

  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
