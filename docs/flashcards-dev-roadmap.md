## 📘 Flashcards App Development Roadmap (Java 17, Spring Boot, PostgreSQL, Docker, CI/CD)

This roadmap breaks your project into clear **learning phases**, each focused on deepening your backend development skills with emphasis on best practices, TDD, DDD, CI/CD, Docker, and deployment.

---

### ✅ Phase 0: Foundation (Completed)
📄 [View details](phase-0-foundation.md)
- [x] Spring Boot backend + PostgreSQL database setup
- [x] Dockerize backend and DB using Docker Compose
- [x] CI Pipeline (GitHub Actions): JDK 17, Maven build, Docker image build, test run
- [x] GitHub Secrets for secure env vars
- [x] Code formatter with git hooks and IDE integration

---

### 🚧 Phase 1: Test-Driven Development (TDD) Mindset
📄 [View details](phase-1-tdd.md)
**Goal:** Shift mindset to TDD-first and ensure tests guide development.

- [x] Write a simple **failing unit test** before writing new logic (red-green-refactor)
- [x] Organize tests using Maven Surefire / JUnit 5 conventions
- [x] Set up testing layers: Unit, Integration, End-to-End
- [ ] Add utility classes for test data (e.g., TestContainers or @TestConfiguration)

> 📌 Tip: Start with a small feature or endpoint and TDD it fully — this becomes your model for future work.

---

### 🎯 Phase 2: Artifact Management & CI Feedback
📄 [View details](phase-2-artifacts-ci-feedback.md)
**Goal:** Store build outputs, generate test reports, and surface quality signals in CI.

- [ ] Configure Maven to produce `.jar` or `.war` artifacts (target directory)
- [ ] Upload built JARs as GitHub Actions artifacts
- [ ] Configure Surefire/Failsafe to output **JUnit XML reports**
- [ ] Upload JUnit test reports to GitHub Actions summary
- [ ] (Optional) Add code coverage reporting (JaCoCo + badge)

---

### 🌍 Phase 3: Integration & Real Environment Testing
📄 [View details](phase-3-integration-tests.md)
**Goal:** Make sure app logic works when talking to real services like Postgres.

- [ ] Use **TestContainers** to spin up a Postgres DB for tests
- [ ] Write integration tests for REST endpoints (using `@SpringBootTest` and `@AutoConfigureMockMvc` or WebTestClient)
- [ ] Ensure tests wait for services (Postgres) to be ready
- [ ] Run integration tests inside Docker Compose via CI

---

### 🚀 Phase 4: Deployment & Delivery
📄 [View details](phase-4-deployment.md)
**Goal:** Learn to deploy the app to the cloud, understand platform trade-offs, and prepare for real-world release cycles.

- [ ] Evaluate Render vs Railway vs Fly.io (pricing, DB support, deploy flow)
- [ ] Choose a platform and deploy the app from Docker image or GitHub repo
- [ ] Configure environment variables and secrets securely
- [ ] Add health check route for uptime monitoring
- [ ] (Optional) Explore CD setup for auto-deploy on merge to `main`

---

### 📄 Phase 5: Documentation & Developer Experience
📄 [View details](phase-5-docs-devx.md)
**Goal:** Improve project presentation and onboarding experience.

- [ ] Write a clean `README.md` with:
  - Project description
  - Tech stack
  - Local setup (Docker/Maven)
  - How to run tests
  - CI/CD explanation
  - Deployment guide
- [ ] Add CI badges: build status, test pass rate, coverage (if added)
- [ ] Consider linking to Javadoc or hosting docs

---

### 📦 Phase 6: Docker Pro Practices
📄 [View details](phase-6-docker-pro.md)
**Goal:** Go beyond basics with Docker image handling and lifecycle.

- [ ] Learn to tag Docker images with Git commit SHA or version
- [ ] Push images to GitHub Container Registry or Docker Hub
- [ ] Use `.dockerignore` to clean up builds
- [ ] Explore multi-stage Docker builds for smaller image sizes
- [ ] (Optional) Use semantic versioning with Git tags + CI workflows
