package com.openai.codegen.utils;

import com.openai.codegen.model.GeneratedCodeModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public interface ApplicationUtils {

    default void addJavaFileToPackage(Path projectDir, String javaCode) throws IOException {
        String packageName = extractPackageName(javaCode);
        String className = extractClassName(javaCode);
        Path packageDir = projectDir.resolve("src/main/java/" + packageName.replace(".", "/"));
        Files.createDirectories(packageDir);
        Files.write(packageDir.resolve(className + ".java"), javaCode.getBytes(StandardCharsets.UTF_8));
    }

    default void addPropertiesFileToDirectory(Path tempDir, GeneratedCodeModel generatedCodeModel) throws IOException {
        Path resourcesPath = tempDir.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);
        Files.write(resourcesPath.resolve("application.properties"),
                generatedCodeModel.getPropertiesFile().getBytes(StandardCharsets.UTF_8));
    }

    default void addPomFileToDirectory(Path tempDir, GeneratedCodeModel generatedCodeModel) throws IOException {
        Path pomPath = tempDir.resolve("pom.xml");
        Files.write(pomPath, generatedCodeModel.getPomFile().getBytes(StandardCharsets.UTF_8));
    }

    default String extractPackageName(String javaCode) {
        for (String line : javaCode.split("\n")) {
            if (line.trim().startsWith("package ")) {
                return line.trim().substring(8, line.trim().length() - 1).trim();
            }
        }
        throw new IllegalArgumentException("Package name not found in class");
    }

    default String extractClassName(String javaCode) {
        for (String line : javaCode.split("\n")) {
            line = line.trim();
            if ((line.startsWith("public class")
                    || line.startsWith("class")
                    || line.startsWith("public interface")
                    || line.startsWith("interface"))
                    && line.contains("{")) {
                String[] parts = line.split("\\s+");
                return parts[2];
            }
        }
        throw new IllegalArgumentException("Class name not found in class");
    }

    default byte[] zipDirectory(Path folderPath) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.walk(folderPath).filter(path -> !Files.isDirectory(path)).forEach(filePath -> {
                String zipEntryName = folderPath.relativize(filePath).toString().replace("\\", "/");
                try {
                    zos.putNextEntry(new ZipEntry(zipEntryName));
                    Files.copy(filePath, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            zos.finish();
            return baos.toByteArray();
        }
    }

}
