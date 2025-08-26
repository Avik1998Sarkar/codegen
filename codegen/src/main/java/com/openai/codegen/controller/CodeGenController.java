package com.openai.codegen.controller;

import com.openai.codegen.service.CodeGenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/codegen")
public class CodeGenController {

    private final CodeGenService codeGenService;

    public CodeGenController(CodeGenService codeGenService) {
        this.codeGenService = codeGenService;
    }

    @PostMapping(value = "/generate", produces = "application/zip")
    public ResponseEntity<byte[]> generate(@RequestBody Map<String, Object> payload) throws IOException {
        byte[] zipBytes = codeGenService.generateCode(payload);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated-project.zip")
                .body(zipBytes);
    }
}
