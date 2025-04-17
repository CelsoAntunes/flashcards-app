# Flashcards Project: Next Steps Plan

## Overview
This document outlines the next steps in the development of the Flashcards project. The goal is to continue progressing with a focus on **TDD** (Test-Driven Development) and **DDD** (Domain-Driven Design). The primary focus now is on relating domains and preparing the system for user-specific content and organization.

---

## Step 1: Relate Flashcards to Users

### Goal:
Establish a relationship between **Users** and their **Flashcards** to allow users to create, view, and manage flashcards. This step sets the foundation for handling user-specific data and begins laying out the user story for flashcard management.

### Actions:
1. **Flashcard Ownership**:
    - Create a relationship between the **User** and **Flashcards** entities.
    - Each flashcard should belong to a specific user, which can be implemented as a `userId` foreign key in the `Flashcard` model.

2. **Flashcard CRUD Operations**:
    - Create services for adding, updating, and deleting flashcards specific to the logged-in user.
    - Write tests for these operations using TDD, focusing on ensuring that users can only modify their own flashcards.

3. **Authentication & Authorization**:
    - Integrate login functionality to authenticate users.
    - Ensure that flashcard creation and modification are tied to a specific user session (user ID linked to logged-in user).

4. **Service Layer**:
    - Build the service layer that manages flashcard logic, ensuring the flashcards are associated with users and ensuring business logic is encapsulated properly.
    - Define the methods: `createFlashcard()`, `getFlashcardsByUser()`, `deleteFlashcard()`, etc.

5. **Testing**:
    - Write unit and integration tests for user-specific flashcard creation and retrieval. For example:
        - **Create Flashcard Test**: Assert that a flashcard is successfully created for a user.
        - **Retrieve Flashcards Test**: Ensure that only the logged-in user’s flashcards are returned.

---

## Step 2: Create Deck Domain

### Goal:
Introduce a **Deck** domain to organize flashcards into groups. Each deck will belong to a user and contain multiple flashcards.

### Actions:
1. **Deck Model**:
    - Create a `Deck` model. A deck should have the following fields:
        - `id`: Unique identifier for the deck.
        - `userId`: User who owns the deck.
        - `name`: Name of the deck (e.g., "Math Flashcards").
        - `flashcards`: List of flashcards that belong to the deck.

2. **Deck CRUD Operations**:
    - Create CRUD operations for decks: creating a deck, adding flashcards to a deck, listing decks, and deleting a deck.
    - Implement services to handle these operations, ensuring they are user-specific (each deck should be linked to a user).

3. **Service Layer**:
    - Implement a service that allows users to manage their decks and add/remove flashcards to/from them.
    - Define the methods: `createDeck()`, `getDecksByUser()`, `addFlashcardToDeck()`, `removeFlashcardFromDeck()`.

4. **Deck-Flashcard Relationship**:
    - Define the relationship between decks and flashcards. A flashcard can belong to one deck, but a deck can contain many flashcards.

5. **Testing**:
    - Write unit and integration tests for deck creation and deck-flashcard management:
        - **Create Deck Test**: Ensure a deck can be created for a user.
        - **Add Flashcard to Deck Test**: Verify flashcards can be added to a deck.
        - **Deck Retrieval Test**: Ensure that the decks retrieved belong to the logged-in user.

---

## Additional Considerations:

1. **Authentication (Login)**:
    - Implement login functionality to authenticate users.
    - After this step, a user should be able to create flashcards, view decks, and perform CRUD operations securely.

2. **Future Features**:
    - **Flashcard Review Logic**: Eventually, you might add features for users to review their flashcards using spaced repetition techniques or quizzes.
    - **User Profile**: Add user-specific settings or profile management as needed.

---

## TDD Focus:

- **Test-Driven Development** should be central in your approach. Write tests for each feature before implementing the corresponding logic.
- Use **unit tests** for smaller components and **integration tests** for interactions between services and repositories.
- Keep the domain logic simple and focused. Each domain (User, Flashcard, Deck) should have a clear responsibility.

---

## Conclusion:
By following this plan, you'll be able to continue building out the Flashcards app with a focus on domain-driven design and test-driven development. The next steps focus on connecting the user domain to the flashcards, followed by introducing the deck functionality to organize the flashcards. Make sure to use TDD to guide your development and keep things aligned with business logic rather than ad-hoc coding.

---

### Best Ways to Keep Track of the Plan:

1. **Documentation**: Regularly update this document or a project board (e.g., Trello, Jira) to track progress.
2. **Checklists**: Break down tasks into smaller checklists within the plan to stay organized.
3. **Version Control**: Use Git or similar version control tools to commit frequently, following the structure and plan outlined above.
4. **Review**: Before starting each step, review the previous steps to ensure proper coverage of the domain and TDD principles.

---
