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

  private Date buildExpiration(long millis) {
    return new Date(System.currentTimeMillis() + millis);
  }

  public String generateAuthToken(String subject, Long userId) {
    return Jwts.builder()
        .subject(subject)
        .claim("userId", userId)
        .claim("type", TokenType.AUTH.name())
        .issuedAt(new Date())
        .expiration(buildExpiration(3600_000))
        .signWith(secretKey)
        .compact();
  }

  public String generateResetToken(String subject, Long userId) {
    return Jwts.builder()
        .subject(subject)
        .claim("userId", userId)
        .claim("type", TokenType.RESET.name())
        .issuedAt(new Date())
        .expiration(buildExpiration(15 * 60 * 1000))
        .signWith(secretKey)
        .compact();
  }

  public void validateToken(String token, TokenType expectedType) {
    if (token == null || token.isBlank()) {
      throw new InvalidTokenException("Token cannot be null or blank");
    }
    try {
      Jws<Claims> claimsJws = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

      Date expiration = claimsJws.getPayload().getExpiration();
      if (expiration != null && expiration.before(new Date())) {
        throw new TokenExpiredException("Token has expired");
      }

      String type = claimsJws.getPayload().get("type", String.class);
      if (!expectedType.name().equals(type)) {
        throw new InvalidTokenException("Unexpected token type");
      }
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Token has expired");
    } catch (JwtException e) {
      throw new InvalidTokenException("Invalid token");
    }
  }

  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
