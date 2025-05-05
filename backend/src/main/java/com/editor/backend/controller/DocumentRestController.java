package com.editor.backend.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.editor.backend.model.Comment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.editor.backend.model.DocumentSession;
import com.editor.backend.service.CRDTService;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    private final DocumentSessionService documentSessions;

    public DocumentRestController(DocumentSessionService documentSessionService) {
        this.documentSessions = documentSessionService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(500).body(error);
    }

    //! USED
    @PostMapping("")
    public ResponseEntity<Map<String, String>> createNewDocument() {
        DocumentSession session = new DocumentSession();
        documentSessions.addDocumentSession(session.getDocId(), session);

        Map<String, String> response = new HashMap<>();
        response.put("documentId", session.getDocId());
        response.put("editorCode", session.getEditorCode());
        response.put("viewerCode", session.getViewerCode());
        System.out.println(session.getEditorCode());
        System.out.println(session.getViewerCode());

        return ResponseEntity.ok(response);
    }

    //! USED
    @PostMapping(path = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> importFile(@RequestBody Map<String, String> payload) {
        System.out.println("[Server] : Start uploading file");

        String fileContent = payload.get("fileContent");
        if (fileContent == null || fileContent.trim().isEmpty()) {
            throw new IllegalArgumentException("fileContent is missing or empty");
        }

        System.out.println("[Server] : File content decoded - " + fileContent);

        // Proceed with CRDT session creation and other logic
        DocumentSession session = new DocumentSession();
        documentSessions.getDocumentSession(session.getDocId());
        CRDTService docCRDT = session.getDocCRDT();
        System.out.println("[Server] : " + fileContent);
        for (char c : fileContent.toCharArray()) {
            System.out.println("[Server] : " + c);
            docCRDT.insertAtCursor(c, "userId", System.currentTimeMillis());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "uploaded successfully");
        response.put("documentId", session.getDocId());
        response.put("editorCode", session.getEditorCode());
        response.put("viewerCode", session.getViewerCode());

        System.out.println("[Server] : Uploaded Document Successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/userJoin/{code}")
    public ResponseEntity<?> undo(@PathVariable String code) {
        Map<String, String> result = documentSessions.getSessionCode(code);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{documentId}/undo")
public ResponseEntity<?> undo(@PathVariable String documentId,
                              @RequestParam("userId") String userId) {
    DocumentSession session = documentSessions.getDocumentSession(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    session.getDocCRDT().undo(userId);

    return ResponseEntity.ok(Map.of("message", "Undo successful"));
}
@PostMapping("/{documentId}/redo")
public ResponseEntity<?> redo(@PathVariable String documentId,
                              @RequestParam("userId") String userId) {
    DocumentSession session = documentSessions.getDocumentSession(documentId);
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
    DocumentSession session = documentSessions.getDocumentSession(documentId);
    if (session == null) {
        throw new IllegalArgumentException("Invalid documentId");
    }

    session.getDocCRDT().updateCursorByIndex(userId, index);

    return ResponseEntity.ok(Map.of("message", "Cursor updated"));
}

    @PostMapping("/{documentId}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable String documentId,
            @RequestParam String userId,
            @RequestParam String content,
            @RequestParam int startIndex,
            @RequestParam int endIndex
    ) {
        DocumentSession session = documentSessions.get(documentId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid documentId");
        }

        session.getDocCRDT().addCommentFromIndexRange(userId, content, startIndex, endIndex);

        return ResponseEntity.ok(Map.of("message", "Comment added"));
    }
    @GetMapping("/{documentId}/comments")
    public ResponseEntity<?> getAllComments(@PathVariable String documentId) {
        DocumentSession session = documentSessions.get(documentId);
        if (session == null) {
            throw new IllegalArgumentException("Invalid documentId");
        }

        List<Comment> comments = session.getDocCRDT().getAllComments();
        return ResponseEntity.ok(comments);
    }
    @GetMapping("/generateUserId")
    public ResponseEntity<Map<String, String>> generateUserId() {
        String userId = UUID.randomUUID().toString(); // or based on auth
        return ResponseEntity.ok(Map.of("userId", userId));
    }
}
