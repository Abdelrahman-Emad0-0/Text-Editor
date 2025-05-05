package com.editor.backend.service;

import org.springframework.stereotype.Service;

import com.editor.backend.model.DocumentSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentSessionService {
    private final Map<String, DocumentSession> documentSessions = new ConcurrentHashMap<>();

    public DocumentSession getDocumentSession(String documentId) {
        return documentSessions.get(documentId);
    }

    public void addDocumentSession(String documentId, DocumentSession documentSession) {
        documentSessions.put(documentId, documentSession);
    }

    public Map<String, String> getSessionCode(String code) {
        Map<String, String> result = new HashMap<>();
        for (DocumentSession session : documentSessions.values()) {
            if (session.getEditorCode().equals(code)) {
                result.put("documentId", session.getDocId());
                result.put("role", "editor");
                return result;
            } else if (session.getViewerCode().equals(code)) {
                result.put("documentId", session.getDocId());
                result.put("role", "viewer");
                return result;
            }
        }
        result.put("documentId", "none");
        result.put("role", "none");
        return result;
    }

    public void removeDocumentSession(String documentId) {
        documentSessions.remove(documentId);
    }

    public boolean documentSessionExists(String documentId) {
        return documentSessions.containsKey(documentId);
    }
}
