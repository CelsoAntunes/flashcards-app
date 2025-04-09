## 📄 Phase 2: Artifact Management & CI Feedback

### 🎯 Goal
Store build outputs, generate test reports, and surface quality signals in CI. Ensure that build artifacts are easily accessible, and tests are visible in the CI pipeline.

---

### 🛠️ What You’ll Learn
- How to store build artifacts (JAR files)
- How to output test reports in CI
- How to improve visibility and quality signals in the CI pipeline

---

### ✅ Tasks
1. **Configure Maven to produce build artifacts**
   - Modify pom.xml to package the application as a .jar or .war file
   - Ensure output goes to the target directory
   - For example, check that your Maven build produces the target/*.jar file.
   
2. **Upload artifacts as GitHub Actions artifacts**
   - In your GitHub Actions workflow, use the actions/upload-artifact step to upload the built JAR or WAR file to the Actions workspace.
   - This is helpful for retaining built outputs that can be used later, such as deploying to another environment or debugging.
   
3. **Configure Maven Surefire/Failsafe to output JUnit XML reports**
   - Modify pom.xml to include configuration for the Maven Surefire Plugin, making it output JUnit XML reports.
   - This enables easy integration with GitHub Actions or other CI platforms, so you can view detailed test reports.
   - Example config:
   ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M5</version>
        <configuration>
            <reportsDirectory>${project.build.directory}/surefire-reports</reportsDirectory>
        </configuration>
    </plugin>
    ```
   
4. **Add JUnit test reports to GitHub Actions summary**
   - In your GitHub Actions workflow, use the upload-artifact step to upload the generated JUnit XML files.
   - This allows GitHub Actions to show test results directly in the summary.

5. **(Optional) Add code coverage reporting**
   - Integrate JaCoCo for code coverage reporting.
   - Modify your pom.xml to add JaCoCo as a plugin, and output coverage reports in the target/site/jacoco/index.html file.
   - You can use a code coverage badge to show the coverage percentage in your README.
   - Example:
    ```xml
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.7</version>
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                    <goal>report</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```

---

### 🧠 Why This Matters

Artifacts and test reports help you keep track of build outputs and test results. Storing artifacts makes it easier to debug issues in deployed environments or CI/CD pipelines. Having test reports available improves visibility into the health of your application at any point in the build cycle.

---

### 🚀 Stretch Goals

- Add a GitHub Actions badge to your README to show the build/test status (successful/failed tests)
- Integrate with a code quality tool like SonarCloud to track quality over time (static analysis, security checks, etc.)

---

### 📎 Resources

- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [GitHub Actions Upload Artifact](https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/storing-and-sharing-data-from-a-workflow)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)