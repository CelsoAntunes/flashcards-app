package com.antunes.flashcards.infrastructure.security;

import com.antunes.flashcards.domain.user.exception.InvalidTokenException;
import com.antunes.flashcards.domain.user.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
  @Value("${spring.jwt.secret}")
  private String secret;

  private SecretKey secretKey;

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

  public String generateToken(String subject, Long userId) {
    return Jwts.builder()
        .subject(subject)
        .claim("userId", userId)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 3600_000))
        .signWith(secretKey)
        .compact();
  }

  public void validateToken(String token) {
    if (token == null || token.isBlank()) {
      throw new InvalidTokenException("Token cannot be null or blank");
    }
    try {
      Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

      Date expiration = claimsJws.getPayload().getExpiration();
      if (expiration != null && expiration.before(new Date())) {
        throw new TokenExpiredException("Token has expired");
      }
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Token has expired");
    } catch (JwtException e) {
      throw new InvalidTokenException("Invalid token");
    }
  }
}
