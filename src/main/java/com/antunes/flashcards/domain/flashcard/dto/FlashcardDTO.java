package com.antunes.flashcards.domain.flashcard.dto;

import com.antunes.flashcards.domain.flashcard.model.Flashcard;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FlashcardDTO {
  private Long id;
  private String question;
  private Long ownerId;

  public static FlashcardDTO fromEntity(Flashcard flashcard) {
    return new FlashcardDTO(
        flashcard.getId(), flashcard.getQuestion(), flashcard.getOwner().getId());
  }

  public static List<FlashcardDTO> fromEntities(List<Flashcard> flashcards) {
    return flashcards.stream().map(FlashcardDTO::fromEntity).collect(Collectors.toList());
  }
}
