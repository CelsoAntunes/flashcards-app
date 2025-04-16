package com.antunes.flashcards.domain.user.repository;

import com.antunes.flashcards.domain.user.model.Email;
import com.antunes.flashcards.domain.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(Email email);
}
