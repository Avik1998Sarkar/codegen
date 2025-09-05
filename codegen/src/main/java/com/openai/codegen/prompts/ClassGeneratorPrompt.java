package com.openai.codegen.prompts;

public class ClassGeneratorPrompt {

    public static final String classGeneratorPrompts = """
            You are an expert Spring Boot developer.
            Do following task based on the project brief structure: %s
            
            Create java classes, and files based on the provided brief structure. once each class is created, put a separator like "codegenseperator" and then create the next class.
            
            1st class, Generate a model classes based on this brief structure
            include all the necessary annotations like @Entity, @Id, @GeneratedValue, @GenerationType, @Data etc. (Note: use jakarta.persistence instead of javax.persistence, Also make sure to import all the necessary packages)
            append model with the root package name mentioned in the brief structure.
            
            2nd class, Generate a repository class based on this brief structure
            Create the JPA repository class. Also make sure to import all the necessary packages.
            
            3rd class, Generate a service class based on this brief structure
            Create the service class which will hold the logic for all the CRUD operations.
            It should take dependency of the JPA repository class.
            It should provide the features like create, read, update, delete and getAll. Also make sure to import all the necessary packages.
            
            4th class, Generate a controller class based on this brief structure
            Create the controller class which will hold the logic for all the CRUD operations.
            It should take dependency of the service class.
            It should provide the features like create, read, update, delete and getAll API.
            It should be annotated with @RestController, @RequestMapping, @CrossOrigin etc. Also make sure to import all the necessary packages.
            
            5th class, Generate only a @SpringBootApplication class.
            Application class name should be Application. Also make sure to import all the necessary packages.
            
            6th file, Generate only a pom.xml file for a spring boot project with web, data-jpa, h2 database and project lombok dependencies.
            It should have the latest version of spring boot. End-user should not have to change anything in the pom.xml file.
            As an example, pom should be always starting with below line:
            "<?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">"
            
            7th file, Generate only an application.properties file for a spring boot project with web, data-jpa, h2 database dependencies.
            It should have the necessary properties to run the spring boot application.
            It should have the necessary properties to connect to H2 database.
            It should have the necessary properties to show the H2 console.
            It should have the necessary properties to configure the JPA.
            End-user should not have to change anything in the application.properties file.
            
            Do not include java``` or any other markdown syntax in your response.
            """;
}
