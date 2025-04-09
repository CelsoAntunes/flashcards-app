## 📄 Phase 6: Docker Pro Practices

### 🎯 Goal
Go beyond the basics of Docker and explore advanced image handling and best practices for managing your Dockerized application. Learn about Docker image versioning, multi-stage builds, and how to optimize your container setup for a production-ready deployment.

---

### 🛠️ What You’ll Learn
- How to manage Docker image versions effectively
- How to use multi-stage Docker builds to optimize image size
- How to push Docker images to container registries
- How to use `.dockerignore` to improve build performance

---

### ✅ Tasks

1. **Learn to tag Docker images with Git commit SHA or version**
    - Tagging your Docker images allows you to track and roll back versions easily. You can tag your images with Git commit SHA, Git tags, or semantic versioning.
    - Example for using the Git commit SHA in the Docker tag:
      ```bash
      docker build -t yourusername/flashcards-app:${GIT_SHA} .
      ```
    - This will tag your image with the current commit SHA, which ensures a unique version for each build.

2. **Push Docker images to GitHub Container Registry or Docker Hub**
    - After building your Docker image, you’ll need to push it to a container registry to make it accessible for deployments.
    - GitHub Container Registry or Docker Hub are common choices.
    - Example to push to Docker Hub:
      ```bash
      docker login
      docker build -t yourusername/flashcards-app:latest .
      docker push yourusername/flashcards-app:latest
      ```
    - Example to push to GitHub Container Registry:
      ```bash
      docker build -t ghcr.io/yourusername/flashcards-app:latest .
      docker push ghcr.io/yourusername/flashcards-app:latest
      ```

3. **Use `.dockerignore` to clean up builds**
    - To optimize your Docker image and avoid unnecessary files being added to the build, create a `.dockerignore` file to exclude files and directories like build artifacts, logs, and configuration files that are not needed in the container.
    - Example `.dockerignore`:
      ```
      .git
      .gitignore
      .mvn
      target/
      *.log
      ```
    - This will speed up the build process and reduce the size of the resulting Docker image.

4. **Explore multi-stage Docker builds**
    - Multi-stage builds allow you to create smaller, more efficient images by separating the build environment from the production environment. This helps you avoid bundling unnecessary build tools or dependencies in your final image.
    - Example of a multi-stage build in a `Dockerfile`:
      ```Dockerfile
      # Build stage
      FROM maven:3.8.1-openjdk-17 AS build
      WORKDIR /app
      COPY . .
      RUN mvn clean package
 
      # Final stage
      FROM openjdk:17-jdk-slim
      WORKDIR /app
      COPY --from=build /app/target/flashcards-app.jar /app/flashcards-app.jar
      CMD ["java", "-jar", "/app/flashcards-app.jar"]
      ```
    - In this setup, the build dependencies (like Maven) are only included in the first stage, and the final image contains just the necessary artifacts (your JAR file) for running the application.

5. **(Optional) Use semantic versioning with Git tags + CI workflows**
    - Implement semantic versioning to make it easier to track and manage Docker image versions in production.
    - You can automate version bumps using Git tags and CI workflows.
    - Example:
        - Create a Git tag (e.g., `v1.0.0`): `git tag v1.0.0`
        - Push the tag to GitHub: `git push origin v1.0.0`
        - In your CI workflow, use the tag as the Docker image version:
          ```yaml
          - name: Build Docker image
            run: docker build -t yourusername/flashcards-app:${GITHUB_REF} .
          ```

---

### 🧠 Why This Matters
Optimizing Docker images and container builds is crucial for efficient, reliable, and maintainable deployments. By managing Docker tags, using multi-stage builds, and pushing images to registries, you ensure that your deployment process is streamlined, secure, and scalable. Using `.dockerignore` helps prevent unnecessary files from being included, reducing image size and build times.

---

### 🚀 Stretch Goals
- Integrate automatic Docker image versioning in your CI pipeline using Git tags or commit SHAs.
- Set up automated deployment triggers from your container registry to the cloud platform upon new image push.
- Use Docker image scanning tools (e.g., Docker Scan or Snyk) to identify vulnerabilities in your images.

---

### 📎 Resources
- [Docker Multi-Stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Ignore Docs](https://docs.docker.com/engine/reference/builder/#dockerignore-file)
- [Semantic Versioning](https://semver.org/)
