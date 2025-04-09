## 📄 Phase 0: Foundation (Completed)

#### 🎯 Goal
Set up the base structure for your Flashcards app with Spring Boot, PostgreSQL, Docker, and CI pipeline. This phase ensures all fundamental components are in place for subsequent development.

---

### 🛠️ Accomplished
- **Spring Boot + PostgreSQL Backend Setup**  
   - Set up Spring Boot project with dependencies for PostgreSQL.
   - Created entities, repositories, and service layers to interact with the DB.

- **Dockerized the Application**  
   - Created `Dockerfile` for both the app and the PostgreSQL database.
   - Configured `docker-compose.yml` to launch the app and DB together.

- **CI Pipeline Setup (GitHub Actions)**  
   - Automated the build, testing, and Docker image creation using GitHub Actions.
   - Set up Maven to compile, run tests, and build a Docker image in the CI pipeline.

- **Environment Variables with GitHub Secrets**  
   - Stored sensitive configuration like database URLs and credentials securely with GitHub Secrets.

- **Code Formatter Setup**  
   - Added a code formatter (via `google-java-format`) and integrated it with `.git/hooks` and IntelliJ IDEA to enforce consistent code style.

---

### 🧠 Why This Matters
This phase ensures that you have a reliable starting point with the basic structure in place, allowing you to focus on actual functionality moving forward without worrying about project setup.

---

### ✅ Done Checklist:
- [x] Spring Boot app initialized with PostgreSQL
- [x] Docker Compose set up with PostgreSQL and app containers
- [x] GitHub Actions CI pipeline for build, test, and Docker image
- [x] GitHub Secrets for secure environment variables
- [x] Code formatter integrated with Git hooks and IDE

---

### 📎 Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Docs](https://docs.docker.com/)
- [GitHub Actions CI](https://docs.github.com/en/actions)
