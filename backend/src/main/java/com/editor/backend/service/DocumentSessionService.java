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

    public void removeDocumentSession(String documentId) {
        documentSessions.remove(documentId);
    }

    public boolean documentSessionExists(String documentId) {
        return documentSessions.containsKey(documentId);
    }
}
