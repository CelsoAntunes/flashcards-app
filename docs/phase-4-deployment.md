## 📄 Phase 4: Deployment & Delivery

### 🎯 Goal
Learn how to deploy the app to the cloud, understand platform trade-offs, and prepare for real-world release cycles. Explore different deployment platforms, set up environment variables, and ensure app health in production.

---

### 🛠️ What You’ll Learn
- How to evaluate and deploy to different cloud platforms (Render, Railway, Fly.io)
- How to manage environment variables securely in production
- How to ensure app health through uptime monitoring and health checks

---

### ✅ Tasks

1. **Evaluate Render vs Railway vs Fly.io**
    - Each platform has its pros and cons, depending on your use case and requirements. Consider factors such as:
        - Pricing
        - Database support (PostgreSQL, Redis, etc.)
        - Deployment flow (Docker vs GitHub repo)
        - Integration with CI/CD
        - Monitoring and scaling options
    - Compare these platforms and select the one that best fits your needs.

2. **Choose a platform and deploy the app**
    - Once you've selected a platform (Render, Railway, or Fly.io), deploy your app from Docker images or directly from your GitHub repository.
    - Follow the platform-specific deployment process (e.g., Docker image push or linking your GitHub repo for automatic deployments).
    - Example:
        - On **Fly.io**, you can deploy directly from your GitHub repo using their `flyctl` CLI.
        - On **Railway**, simply link your GitHub repo and deploy with a few clicks.
        - On **Render**, link your repo and deploy the Docker container.

3. **Configure environment variables and secrets securely**
    - Set up production environment variables and secrets securely through the platform’s environment settings.
    - Ensure sensitive information (e.g., database credentials, API keys) is never hardcoded and always stored securely.
    - Most platforms offer a way to configure secrets either through their web UI or CLI.
    - Example for **Render**:
        - Navigate to the service dashboard → Environment → Add environment variables.

4. **Add a health check route for uptime monitoring**
    - A health check endpoint ensures that the platform can monitor the app’s uptime and functionality.
    - Implement a simple endpoint like `/health` that returns a 200 OK status when the app is running correctly.
    - Example:
      ```java
      @RestController
      public class HealthCheckController {
 
          @GetMapping("/health")
          public ResponseEntity<String> healthCheck() {
              return ResponseEntity.ok("App is healthy!");
          }
      }
      ```

5. **(Optional) Explore CD setup for auto-deploy on merge to `main`**
    - Set up Continuous Deployment (CD) to automatically deploy the app to your chosen platform when changes are merged into the `main` branch.
    - This can usually be done by linking your GitHub repository to the platform and configuring automatic deploys on every push to `main`.

---

### 🧠 Why This Matters
Deploying the app to a cloud platform is essential for making it available to users and ensuring that it’s resilient and scalable. Understanding how to deploy, manage environment variables, and monitor app health are key steps in running a production system.

---

### 🚀 Stretch Goals
- Set up monitoring and logging tools (e.g., LogRocket, New Relic) to track performance and errors.
- Implement scaling strategies (e.g., horizontal scaling, autoscaling) depending on platform capabilities.
- Set up custom domains and HTTPS for secure communication.

---

### 📎 Resources
- [Render Docs](https://render.com/docs)
- [Railway Docs](https://docs.railway.app/)
- [Fly.io Docs](https://fly.io/docs/)
- [Health Check in Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-health)
