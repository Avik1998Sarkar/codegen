package com.openai.codegen.model;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedCodeModel {
    private String modelClass;
    private String controllerClass;
    private String serviceClass;
    private String repositoryClass;
    private String applicationClass;
    private String pomFile;
    private String propertiesFile;
}
