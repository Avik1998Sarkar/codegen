package com.openai.codegen.prompts;

public class ClassGeneratorPrompt {

    public static final String classGeneratorPrompts = """
                     You are an expert Spring Boot developer.
                     Do following task based on the project brief structure: %s
            
                     Create java classes, and files based on the provided brief structure. once each class is created, put a separator like "codegenseperator" and then create the next class.
                     All the java classes should start with "package", should not start with any other text.
                     for pom and properties also, those files should not start with any other text except the actual code.
            
                     1st class, Generate a model classes based on this brief structure
                     Make sure to include all the necessary getter setter constructors and toString methods, no need to use lombok annotations. Also include all the necessary annotations like @Table, @Column etc. whatever is needed from com.google.cloud.spring.data.spanner.core.mapping package. Also make sure to import all the necessary packages)
                     append model with the root package name mentioned in the brief structure.
            
                     2nd class, Generate a repository class based on this brief structure
                     Create the spanner repository class. Also make sure to import all the necessary packages. for spanner repository use com.google.cloud.* packages.
            
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
            
                     6th file, Generate only a pom.xml file for a spring boot project with web, also include dependency for spanner connection. like below:
            
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
            
                     7th file, Generate only an application.properties file for a spring boot project with web, dependency for spanner connection.
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
            
                     Do not include java``` or any other markdown syntax in your response.
            """;
}
