package com.editor.backend.service;

import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.editor.backend.model.DocumentSession;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    // Documnet Structure to Preserve Each Session
    private static final Map<String, DocumentSession> documentSessions = new HashMap<>();

    // Global Exception Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(500).body(error);
    }

    @PostMapping("")
    public ResponseEntity<Map<String, String>> createNewDocument() {
        DocumentSession session = new DocumentSession();
        documentSessions.put(session.getDocId(), session);
        // * Returns A new Json Response With DocId, editorCode, ViewerCode
        Map<String, String> response = new HashMap<>();
        response.put("documentId", session.getDocId());
        response.put("editorCode", session.getEditorCode());
        response.put("viewerCode", session.getViewerCode());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/")
    public ResponseEntity<Map<String, String>> importFile (@RequestParam("file") MultipartFile file, @RequestBody Map<String, String> body) {
        // TODO: Upload File Into A CRDT

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is missing or empty");
        }

        if (!file.getContentType().equals("text/plain")) {
            throw new IllegalArgumentException("File Type is not text");
        }

        String fileContent;
        try {
            fileContent = new String(file.getBytes());
        } catch(Exception e) {
            throw new RuntimeException("Error Reading File");
        }

        Map<String, String> response = new HashMap<>();
        DocumentSession session = new DocumentSession();
        CRDTService docCRDT = session.getDocCRDT();

        for (char c : fileContent.toCharArray()) {
            // Still need the parentID
            // docCRDT.insert(c, , body.get("userId"),  System.currentTimeMillis());
        }

        documentSessions.put(session.getDocId(), session);
        response.put("message", "uploaded sucessfully");
        return ResponseEntity.ok(response);
    }
}
