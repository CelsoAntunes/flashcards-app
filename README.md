# Flashcards App

This is a personal project focused on building a Flashcards application using Java and Spring Boot.

## 🚀 Project Goals

The main objective of this project is to create a robust and flexible flashcards system where users can:

- Create, update, and delete flashcards
- Group flashcards into decks
- Track study progress and performance
- Utilize spaced repetition for efficient learning

## 📅 Roadmap

For a detailed explanation of the project roadmap, please refer to the [Roadmap Documentation](./docs/flashcards-dev-roadmap.md).

## 📁 Project Status

This project is currently in active development and **not yet ready for production use**. Stay tuned for updates as the core features are implemented.

## 🧠 Why This Project?

The goal is not just to build a flashcard app, but to:

- Practice clean architecture and good software engineering practices
- Deepen knowledge of Java and Spring Boot
- Learn how to design maintainable APIs
- Explore deployment, CI/CD, and documentation practices

## 📁 Project Structure

Here is a breakdown of the project structure:

```
├── main
│   ├── java/com/antunes/flashcards
│   │   ├── controller
│   │   ├── domain
│   │   │   ├── flascard
│   │   │   │   ├── exception
│   │   │   │   ├── model
│   │   │   │   ├── repository
│   │   │   │   ├── service
│   │   │   │   └── validation
│   │   │   └── user
│   │   │       ├── auth
│   │   │       │   ├── login
│   │   │       │   └── token
│   │   │       ├── exception
│   │   │       ├── model
│   │   │       ├── repository
│   │   │       ├── service
│   │   │       └── validation
│   │   ├── exception
│   │   └── infrastructure
│   │       └── config
│   └── resources
│       ├── static
│       └── templates
└── test
    └── java/com/antunes/flashcards
        ├── controller
        └── domain
            ├── flashcard
            │   ├── repository
            │   ├── service
            │   └── validation
            └── user
                 ├── auth
                 │   ├── login
                 │   └── token
                 ├── model
                 └── service
```

## 🛠 Technologies Used

- **Java 17+**
- **Spring Boot**
- **JUnit 5**
- **Mockito**
- **JWT (JSON Web Tokens)**
- **PostgreSQL**

---

Feel free to fork, follow, or contribute if you find this interesting!
