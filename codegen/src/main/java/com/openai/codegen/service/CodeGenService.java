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
        String serviceCode = generateServiceClass(jsonString, modelCode);
        String controllerCode = generateControllerClass(jsonString, modelCode, serviceCode);
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
                - Spanner Database connection is configured in `application.properties`.
                
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
                Make sure to create the class name same as the file name. Example: if file name is Students.java then class name should be Students.
                Make sure to include all the necessary getter setter constructors and toString methods.
                Also include all the necessary annotations like @Table, @Column etc. whatever is needed from com.google.cloud.spring.data.spanner.core.mapping package.
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
                Make sure to create the class name same as the file name. Example: if file name is StudentsRepository.java then class name should be StudentsRepository.
                Only provide the code and nothing else.
                Create the spanner repository class.
                Also make sure to import all the necessary packages. for spanner repository use com.google.cloud.* packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string);
        return chatClient.prompt(repoPrompt).call().content();
    }

    private String generateServiceClass(String string, String modelCode) {
        String servicePrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a service class based on this json schema: %s
                refer model class: %s
                Make sure to create the class name same as the file name. Example: if file name is StudentsService.java then class name should be StudentsService.
                Only provide the code and nothing else.
                Create the service class which will hold the logic for all the CRUD operations.
                It should take dependency of the spanner repository class.
                It should provide the features like create, read, update, delete and findAll(should return list of items, if required iterate through the iterator and provide the response).
                
                Example method signature for findAll: public List<Student> findAllStudent() {}
                Example method signature for create: public Student createBook(Student book) {}
                Example method signature for update: public Student updateBook(String id, Student book) {}
                Example method signature for delete: public void deleteBook(String id) {}
                Example method signature for get by id: public Student getBookById(String id) {}
                
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string, modelCode);
        return chatClient.prompt(servicePrompt).call().content();
    }

    private String generateControllerClass(String string, String modelClass, String serviceClass) {
        String controllerPrompt = String.format("""
                You are an expert Spring Boot developer.
                Generate a controller class based on this json schema: %s
                refer model class: %s
                refer service class: %s
                Make sure to create the class name same as the file name. Example: if file name is StudentsController.java then class name should be StudentsController.
                Only provide the code and nothing else.
                Create the controller class which will hold the logic for all the CRUD operations.
                It should take dependency of the service class.
                Example method signature for findAll: @GetMapping("/books") public List<Student> findAllStudent() {}
                Example method signature for create: @PostMapping("/books") public Student createBook(@RequestBody Student book) {}
                Example method signature for update: @PutMapping("/books/{id}") public Student updateBook(@PathVariable String id, @RequestBody Student book) {}
                Example method signature for delete: @DeleteMapping("/books/{id}") public void deleteBook(@PathVariable String id) {}
                Example method signature for get by id: @GetMapping("/books/{id}") public Student getBookById(@PathVariable String id) {}
                It should provide the features like create, read, update, delete and getAll API.
                It should be annotated with @RestController, @RequestMapping, @CrossOrigin etc.
                Also make sure to import all the necessary packages.
                Include the package name like mentioned on rootPackage variable. Include model/repository/service/controller if needed.
                Do not include java``` or any other markdown syntax in your response.
                """, string, modelClass, serviceClass);
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
                Generate only a pom.xml file for a spring boot project with web, project lombok dependencies.
                also include dependency for spanner connection. like below:
                
                <dependency>
                    <groupId>com.google.cloud</groupId>
                    <artifactId>spring-cloud-gcp-starter-data-spanner</artifactId>
                    <version>7.2.0</version> (7.2.0 is the latest version as of now, please use the latest version available)
                </dependency>
                
                Only provide the code and nothing else.
                It should have the latest version of spring boot(right now 3.5.5 is the latest version, please use the latest version available) and java(21 is the latest version as of now, please use the latest version available).
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
                Generate only an application.properties file for a spring boot project with web, dependency for spanner connection and project lombok dependencies.
                Only provide the code and nothing else.
                It should have the necessary properties for connecting to a spanner database.
                Should have properties for placing credentials file path, project id, instance id and database id like below:
                
                # Spanner configuration
                spring.datasource.platform=spanner
                spring.cloud.gcp.spanner.credentials.location=file:/path/to/credentials.json
                spring.cloud.gcp.spanner.project-id=your-project-id
                spring.cloud.gcp.spanner.instance-id=your-instance-id
                spring.cloud.gcp.spanner.database=your-database-id
                
                # Enable the spanner module
                spring.cloud.gcp.spanner.enabled=true
                
                # Additional connection pool settings
                spring.datasource.initialization-mode=always
                spring.datasource.url=jdbc:cloudspanner:/projects/${spring.cloud.gcp.spanner.project-id}/instances/${spring.cloud.gcp.spanner.instance-id}/databases/${spring.cloud.gcp.spanner.database}
                
                End-user should not have to change anything in the application.properties file.
                Also add server port property to run the application on port 8080.
                and all the  necessary properties to run a spring boot application.
                Do not include java``` or any other markdown syntax in your response.
                """;
        return chatClient.prompt(propsPrompt).call().content();
    }
}
