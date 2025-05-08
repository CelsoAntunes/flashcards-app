package com.antunes.flashcards.domain.user.dto;

import com.antunes.flashcards.domain.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {
  private Long id;
  private String name;
  private String email;

  public static UserDTO fromEntity(User user) {
    return new UserDTO(user.getId(), user.getUsername(), user.getEmail().getValue());
  }
}
