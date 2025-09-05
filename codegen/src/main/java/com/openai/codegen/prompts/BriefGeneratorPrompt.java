package com.openai.codegen.prompts;

public class BriefGeneratorPrompt {
    public static final String briefGeneratorPrompts = """
            You are an expert Spring Boot developer.
            Provide a brief structure of this json schema: %s
            
            i.e. what is the class name, what are the attributes and their types.
            if there are any relationships like one-to-many, many-to-one etc.
            or if we have any inner classes etc. and same attributes should be grouped together.
            Also provide the root package name.
            Only provide the brief and nothing else.
            Do not include java``` or any other markdown syntax in your response.
            """;
}
