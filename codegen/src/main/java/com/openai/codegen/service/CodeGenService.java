package com.openai.codegen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CodeGenService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeGenService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public byte[] generateCode(Object modelString) throws IOException {
        String jsonString = objectMapper.writeValueAsString(modelString);

        // Extract rootPackage from JSON
        JsonNode root = objectMapper.readTree(jsonString);
        String rootPackage = root.get("rootPackage").asText();
        String packagePath = rootPackage.replace(".", "/");

        // Create temp dir for project
        Path tempDir = Files.createTempDirectory("codegen-project");

        // Generate classes from OpenAI
        String modelCode = generateModelClass(jsonString);
        String repoCode = generateRepositoryClass(jsonString);
        String serviceCode = generateServiceClass(jsonString);
        String controllerCode = generateSControllerClass(serviceCode);
        String appClass = generateSpringBootAppClass(jsonString);
        String props = generatePropertiesFile();
        String pom = generatePOMXML();
        String readme = generateReadme(rootPackage);

        // Write files
        writeFile(tempDir, "pom.xml", pom);
        writeFile(tempDir, "README.md", readme);
        writeFile(tempDir, "src/main/resources/application.properties", props);
        writeFile(tempDir, "src/main/java/" + packagePath + "/model/Books.java", modelCode);
        writeFile(tempDir, "src/main/java/" + packagePath + "/repository/BooksRepository.java", repoCode);
        writeFile(tempDir, "src/main/java/" + packagePath + "/service/BooksService.java", serviceCode);
        writeFile(tempDir, "src/main/java/" + packagePath + "/controller/BooksController.java", controllerCode);
        writeFile(tempDir, "src/main/java/" + packagePath + "/ProjectApplication.java", appClass);

        // Zip project
        Path zipPath = Files.createTempFile("codegen-", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipPath)))) {
            Files.walk(tempDir).filter(Files::isRegularFile).forEach(file -> {
                try {
                    Path relative = tempDir.relativize(file);
                    zos.putNextEntry(new ZipEntry(relative.toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        return Files.readAllBytes(zipPath);
    }

    private void writeFile(Path baseDir, String relativePath, String content) throws IOException {
        Path filePath = baseDir.resolve(relativePath);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    private String generateReadme(String rootPackage) {
        return """
                # Generated Spring Boot Project
                
                ## How to Run
                1. Navigate to the project directory.
                2. Run using Maven:
                   ```bash
                   mvn spring-boot:run
                   ```
                3. Application starts on **http://localhost:8080**
                
                ## Database
                - In-memory H2 database
                - H2 console available at: `/h2-console`
                
                ## CRUD Endpoints Example
                - `POST /books` - Create a book
                - `GET /books` - List all books
                - `GET /books/{id}` - Get a book by ID
                - `PUT /books/{id}` - Update a book
                - `DELETE /books/{id}` - Delete a book
                
                ## Root Package
                """ + rootPackage;
    }

    private String generateModelClass(String string) {
        String modelPrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a model class based on this json schema: %s
                Make sure to include all the necessary getter setter constructors and toString methods.
                Also include all the necessary annotations like @Entity, @Id, @GeneratedValue, @GenerationType, @Data etc.
                use jakarta.persistence instead of javax.persistence
                Only provide the code and nothing else.
                Create the model class Name based on the json schema above.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string);
        return chatClient.prompt(modelPrompt).call().content();
    }

    private String generateRepositoryClass(String string) {
        String repoPrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a repository class based on this json schema: %s
                Only provide the code and nothing else.
                Create the JPA repository class.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string);
        return chatClient.prompt(repoPrompt).call().content();
    }

    private String generateServiceClass(String string) {
        String servicePrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a service class based on this json schema: %s
                Only provide the code and nothing else.
                Create the service class which will hold the logic for all the CRUD operations.
                It should take dependency of the JPA repository class.
                It should provide the features like create, read, update, delete and getAll.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string);
        return chatClient.prompt(servicePrompt).call().content();
    }

    private String generateSControllerClass(String string) {
        String controllerPrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a controller class based on this service class: %s
                Only provide the code and nothing else.
                Create the controller class which will hold the logic for all the CRUD operations.
                It should take dependency of the service class.
                It should provide the features like create, read, update, delete and getAll API.
                It should be annotated with @RestController, @RequestMapping, @CrossOrigin etc.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string);
        return chatClient.prompt(controllerPrompt).call().content();
    }

    private String generateSpringBootAppClass(String jsonString) {
        String appPrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate only a @SpringBootApplication class.
                Only take the rootPackage variable from this json schema: %s
                Application class name should be ProjectApplication.
                it should be public class ProjectApplication only
                Only provide the code and nothing else.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable.
                Do not include java``` or any other markdown syntax in your response.
                """, jsonString);
        return chatClient.prompt(appPrompt).call().content();
    }

    private String generatePOMXML() {
        String pomPrompt = """
                You are an expert Spring Boot developer.
                Generate only a pom.xml file for a spring boot project with web, data-jpa, h2 database and project lombok dependencies.
                Only provide the code and nothing else.
                It should have the latest version of spring boot.
                End-user should not have to change anything in the pom.xml file.
                As an example, pom should be always starting with below line:
                
                "<?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">"
                
                Do not include java``` or pom tags or any other markdown syntax in your response.
                """;
        return chatClient.prompt(pomPrompt).call().content();
    }

    private String generatePropertiesFile() {
        String propsPrompt = """
                You are an expert Spring Boot developer.
                Generate only an application.properties file for a spring boot project with web, data-jpa, h2 database dependencies.
                Only provide the code and nothing else.
                It should have the necessary properties to run the spring boot application.
                It should have the necessary properties to connect to H2 database.
                It should have the necessary properties to show the H2 console.
                It should have the necessary properties to configure the JPA.
                End-user should not have to change anything in the application.properties file.
                Do not include java``` or any other markdown syntax in your response.
                """;
        return chatClient.prompt(propsPrompt).call().content();
    }
}
