## 📄 Phase 3: Integration & Real Environment Testing

### 🎯 Goal
Make sure app logic works when interacting with real services like PostgreSQL. Use integration tests to validate that components communicate properly in the actual environment.

---

### 🛠️ What You’ll Learn
- How to set up and use **TestContainers** for Postgres DB in tests
- How to write **integration tests** for REST endpoints
- How to ensure services are ready before running tests

---

### ✅ Tasks

1. **Use TestContainers to spin up a Postgres DB for tests**
    - TestContainers allows you to easily spin up a Postgres database in a Docker container for testing purposes.
    - In your tests, use the `@Container` annotation and a `PostgreSQLContainer` to launch a temporary DB instance.
    - Example:
      ```java
      import org.testcontainers.containers.PostgreSQLContainer;
      import org.junit.jupiter.api.Test;
      import org.junit.jupiter.api.extension.ExtendWith;
      import org.springframework.test.context.junit.jupiter.SpringExtension;
 
      @ExtendWith(SpringExtension.class)
      public class PostgresIntegrationTest {
 
          @Container
          public PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:13")
              .withDatabaseName("test")
              .withUsername("user")
              .withPassword("password");
 
          @Test
          public void testPostgresConnection() {
              // Your test logic here, e.g., validating DB connection
          }
      }
      ```
    - Ensure that the Postgres DB is started and available before running tests.

2. **Write integration tests for REST endpoints**
    - Use `@SpringBootTest` to load the full Spring context and test real endpoints with a real database connection.
    - Use `@AutoConfigureMockMvc` to mock the MVC layer and send HTTP requests to test APIs.
    - Example:
      ```java
      @SpringBootTest
      @AutoConfigureMockMvc
      public class FlashcardControllerTest {
 
          @Autowired
          private MockMvc mockMvc;
 
          @Test
          public void testCreateFlashcard() throws Exception {
              mockMvc.perform(post("/flashcards")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("{\"question\": \"What is 2+2?\", \"answer\": \"4\"}"))
                      .andExpect(status().isCreated())
                      .andExpect(jsonPath("$.question").value("What is 2+2?"));
          }
      }
      ```

3. **Ensure tests wait for services (Postgres) to be ready**
    - Use `@BeforeAll` or `@BeforeEach` annotations to ensure that the database container is fully started before running tests.
    - You can use TestContainers' built-in methods like `postgres.getMappedPort()` to check if the DB is up and ready for connections.
    - Example:
      ```java
      @BeforeAll
      public static void setUp() {
          // Ensure the DB container is up and running
          postgres.start();
      }
      ```

4. **Run integration tests inside Docker Compose via CI**
    - Add a step in your GitHub Actions workflow to spin up your services (e.g., Postgres) using Docker Compose.
    - You can use the `docker-compose` command in your CI pipeline to start services, then run the tests.
    - Example (GitHub Actions step):
      ```yaml
      jobs:
        test:
          runs-on: ubuntu-latest
          services:
            postgres:
              image: postgres:13
              ports:
                - 5432:5432
              env:
                POSTGRES_USER: user
                POSTGRES_PASSWORD: password
                POSTGRES_DB: test
          steps:
            - name: Checkout code
              uses: actions/checkout@v2
 
            - name: Set up JDK 17
              uses: actions/setup-java@v2
              with:
                java-version: 17
 
            - name: Build with Maven
              run: mvn clean install
 
            - name: Run tests
              run: mvn test
      ```

---

### 🧠 Why This Matters
Integration tests ensure that your app works as expected when interacting with real services. They are essential for verifying that components (e.g., database, APIs) interact correctly in a realistic environment. By testing with TestContainers and running tests in a Dockerized CI environment, you create a reliable and repeatable test process.

---

### 🚀 Stretch Goals
- Add integration tests for other services like Redis or external APIs, if applicable.
- Explore testing asynchronous processes or events, such as messaging queues (e.g., Kafka, RabbitMQ).

---

### 📎 Resources
- [TestContainers Documentation](https://java.testcontainers.org/)
- [Spring Boot Test Documentation](https://docs.spring.io/spring-boot/how-to/testing.html)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
