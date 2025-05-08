package com.antunes.flashcards.domain.deck.dto;

import com.antunes.flashcards.domain.deck.model.Deck;
import com.antunes.flashcards.domain.flashcard.dto.FlashcardDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeckDTO {
  private Long id;
  private String title;
  private int size;
  private List<FlashcardDTO> flashcards;
  private Long ownerId;

  public static DeckDTO fromEntity(Deck deck) {
    List<FlashcardDTO> flashcardDTOs =
        deck.getFlashcards().stream().map(FlashcardDTO::fromEntity).collect(Collectors.toList());

    return new DeckDTO(
        deck.getId(), deck.getTitle(), deck.size(), flashcardDTOs, deck.getOwner().getId());
  }

  public static List<DeckDTO> fromEntities(List<Deck> decks) {
    return decks.stream().map(DeckDTO::fromEntity).collect(Collectors.toList());
  }
}
