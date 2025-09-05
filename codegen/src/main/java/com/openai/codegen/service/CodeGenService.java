package com.openai.codegen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.codegen.model.GeneratedCodeModel;
import com.openai.codegen.utils.ApplicationUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

import static com.openai.codegen.prompts.BriefGeneratorPrompt.briefGeneratorPrompts;
import static com.openai.codegen.prompts.ClassGeneratorPrompt.classGeneratorPrompts;

@Service
public class CodeGenService implements ApplicationUtils {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeGenService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public byte[] generateCode(Object modelString) throws IOException {
        String generateBrief = generateBrief(objectMapper.writeValueAsString(modelString));
        String generatedClasses = generateClasses(generateBrief);

        GeneratedCodeModel generatedCodeModel = getGeneratedCodeModel(generatedClasses);

        Path tempDir = Files.createTempDirectory("spring-boot-project");
        addJavaFileToPackage(tempDir, generatedCodeModel.getModelClass());
        addJavaFileToPackage(tempDir, generatedCodeModel.getRepositoryClass());
        addJavaFileToPackage(tempDir, generatedCodeModel.getServiceClass());
        addJavaFileToPackage(tempDir, generatedCodeModel.getControllerClass());
        addJavaFileToPackage(tempDir, generatedCodeModel.getApplicationClass());
        addPomFileToDirectory(tempDir, generatedCodeModel);
        addPropertiesFileToDirectory(tempDir, generatedCodeModel);

        return zipDirectory(tempDir);
    }

    private String generateBrief(String string) {
        String briefGeneratorPrompt = String.format(briefGeneratorPrompts, string);
        return chatClient.prompt(briefGeneratorPrompt).call().content();
    }

    private String generateClasses(String briefJsonStructure) {
        String classGeneratorPrompt = String.format(classGeneratorPrompts, briefJsonStructure);
        return chatClient.prompt(classGeneratorPrompt).call().content();
    }

    private static GeneratedCodeModel getGeneratedCodeModel(String generatedClasses) {
        GeneratedCodeModel generatedCodeModel = new GeneratedCodeModel();
        String[] classes = generatedClasses.split("codegenseperator");
        try {
            generatedCodeModel.setModelClass(classes[0].trim());
            generatedCodeModel.setRepositoryClass(classes[1].trim());
            generatedCodeModel.setServiceClass(classes[2].trim());
            generatedCodeModel.setControllerClass(classes[3].trim());
            generatedCodeModel.setApplicationClass(classes[4].trim());
            generatedCodeModel.setPomFile(classes[5].trim());
            generatedCodeModel.setPropertiesFile(classes[6].trim());
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new RuntimeException("Service generation failed. Please try again");
        }
        return generatedCodeModel;
    }
}
