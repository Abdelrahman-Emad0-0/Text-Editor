package com.editor.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.editor.backend.model.DocumentSession;
import com.editor.backend.service.CRDTService;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    private static final Map<String, DocumentSession> documentSessions = new ConcurrentHashMap<>();

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

        Map<String, String> response = new HashMap<>();
        response.put("documentId", session.getDocId());
        response.put("editorCode", session.getEditorCode());
        response.put("viewerCode", session.getViewerCode());

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/upload/", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentId") String documentId,
            @RequestParam("userId") String userId) {

        // TODO: Upload File Into A CRDT

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is missing or empty");
        }

        // TODO: Optional file type validation
        // if (!file.getContentType().equals("text/plain")) {
        //     throw new IllegalArgumentException("File Type is not text");
        // }

        DocumentSession session = documentSessions.get(documentId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid documentId");
        }

        String fileContent;
        try {
            fileContent = new String(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error Reading File");
        }

        CRDTService docCRDT = session.getDocCRDT();

        for (char c : fileContent.toCharArray()) {
            docCRDT.insertAtCursor(c, userId, System.currentTimeMillis());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "uploaded successfully");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{documentId}")
public ResponseEntity<Map<String, String>> getDocument(@PathVariable String documentId) {
    DocumentSession session = documentSessions.get(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    String content = session.getDocCRDT().getDocument();

    Map<String, String> response = new HashMap<>();
    response.put("document", content);

    return ResponseEntity.ok(response);
}
@PostMapping("/{documentId}/undo")
public ResponseEntity<?> undo(@PathVariable String documentId,
                              @RequestParam("userId") String userId) {
    DocumentSession session = documentSessions.get(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    session.getDocCRDT().undo(userId);

    return ResponseEntity.ok(Map.of("message", "Undo successful"));
}
@PostMapping("/{documentId}/redo")
public ResponseEntity<?> redo(@PathVariable String documentId,
                              @RequestParam("userId") String userId) {
    DocumentSession session = documentSessions.get(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    session.getDocCRDT().redo(userId);

    return ResponseEntity.ok(Map.of("message", "Redo successful"));
}
@PostMapping("/{documentId}/cursor")
public ResponseEntity<?> updateCursor(@PathVariable String documentId,
                                      @RequestParam String userId,
                                      @RequestParam int index) {
    DocumentSession session = documentSessions.get(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    session.getDocCRDT().updateCursorByIndex(userId, index);

    return ResponseEntity.ok(Map.of("message", "Cursor updated"));
}

}
