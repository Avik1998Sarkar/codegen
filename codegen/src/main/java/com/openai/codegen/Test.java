package com.openai.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Test {

    public static void main(String[] args) {
        String newClass = """
                package com.new.custompackagename;
                
                public class NewClass {
                    // class implementation
                }
                """;

        String newClass2 = """
                package com.new.custompackagename.subpackage;
                
                public class NewClassTwo {
                    // class implementation
                }
                """;

        // Base output directory
        String baseDir = "output";

        try {
            Path zipPath = Paths.get(baseDir, "source.zip");
            Files.createDirectories(Paths.get(baseDir));

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                writeAndZipClass(newClass, baseDir, zos);
                writeAndZipClass(newClass2, baseDir, zos);
            }

            System.out.println("ZIP created at: " + zipPath.toAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeAndZipClass(String classContent, String baseDir, ZipOutputStream zos) throws IOException {
        // Extract package name
        String packageLine = classContent.split("\n")[0].trim();
        String packageName = packageLine.replace("package", "").replace(";", "").trim();
        String packagePath = packageName.replace(".", File.separator);

        // Extract class name using regex
        String className = extractClassName(classContent);
        String fileName = className + ".java";

        // Create folder structure
        File packageDir = new File(baseDir, packagePath);
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        // Write file to disk
        File javaFile = new File(packageDir, fileName);
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(classContent);
        }

        // Add to zip
        String zipEntryName = packagePath.replace(File.separator, "/") + "/" + fileName;
        zos.putNextEntry(new ZipEntry(zipEntryName));
        Files.copy(javaFile.toPath(), zos);
        zos.closeEntry();

        System.out.println("Added to ZIP: " + zipEntryName);
    }

    private static String extractClassName(String classContent) {
        for (String line : classContent.split("\n")) {
            line = line.trim();
            if (line.startsWith("class ") || line.contains(" class ")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("class")) {
                        return parts[i + 1];
                    }
                }
            }
        }
        throw new IllegalArgumentException("No class name found in content");
    }
}
