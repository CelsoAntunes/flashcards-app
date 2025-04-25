package com.antunes.flashcards.domain.user.auth.repository;

import com.antunes.flashcards.domain.user.auth.model.LoginAttempt;
import com.antunes.flashcards.domain.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
  Optional<LoginAttempt> findByUser(User user);
}
