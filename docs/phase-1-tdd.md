## 📄 Phase 1: Test-Driven Development (TDD) Mindset

#### 🎯 Goal
Adopt a test-first development approach. Understand how writing tests before implementation leads to more maintainable, reliable, and purpose-driven code.

---

### 🛠️ What You’ll Learn
- The Red-Green-Refactor cycle
- How to structure test layers (unit, integration, end-to-end)
- How to use JUnit 5 with Spring Boot
- How to run tests via Maven and CI
- How to write tests that communicate intent clearly

---

### ✅ Tasks

1. **Write a simple failing unit test**  
   Example:
   ```java
   @Test
   void shouldCreateNewFlashcard() {
       Flashcard card = new FlashcardService().create("What is Java?", "A programming language.");
       assertEquals("What is Java?", card.getQuestion());
   }
   ```
   This test will fail because the service and method don’t exist yet.

2. **Implement just enough code to make it pass**
   - Create `Flashcard`, `FlashcardService`, and `create(...)`
   - Hardcode the result if necessary — refactor later

3. **Organize your tests into layers**
   - `/src/test/java/...` for unit tests
   - `/src/integrationTest/...` or tag integration tests with `@Tag("integration")`

4. **Configure Maven for testing**
   - Ensure the `maven-surefire-plugin` is set up
   - Use lifecycle goals: `test`, `verify`

5. **Explore Spring Boot test features**
   - `@SpringBootTest` for loading the context
   - `@MockBean` to mock dependencies
   - `@DataJpaTest` for DB-layer tests

---

### 🧠 Why This Matters
Writing tests first forces you to think about usage and edge cases from the start. It also helps keep your code modular, and CI-friendly.

---

### 🚀 Stretch Goals
- Add a test utility class to generate test data
- Mock services or DB interactions using Mockito
- Parameterize tests with `@ParameterizedTest`

---

### 📎 Resources
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
