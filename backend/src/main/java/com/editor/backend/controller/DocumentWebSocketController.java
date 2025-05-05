package com.editor.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.editor.backend.model.DocumentSession;
import com.editor.backend.model.Operation;
import com.editor.backend.model.User;
import com.editor.backend.service.CRDTService;
import com.editor.backend.service.DocumentSessionService;
import com.editor.backend.service.CRDTService;

public class DocumentWebSocketController {

    private final DocumentSessionService documentSessionService;

    public DocumentWebSocketController(DocumentSessionService documentSessionService) {
        this.documentSessionService = documentSessionService;
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        System.out.println("User Connected To the Session");
    }

    // TODO : Remove User Id from the Document
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("[Server] : User Disconnected disconnected from document");
    }

    // TODO : Complete User Data
    @MessageMapping("/connectUser/{documentId}")
    @SendTo("/topic/Document/{documentId")
    public Map<String, String> addUser(@DestinationVariable String documentId) {
        Map<String, String> response = new HashMap<>();
        User newUser = new User("");
        documentSessionService.getDocumentSession(documentId).addToUsers(newUser);
        response.put("userId", "actual User Id");
        response.put("Name", "Anoynoumus");
        return response;
    }

    @MessageMapping("/updateDocument/{documentId}")
    @SendTo("/topic/Document/{documentId}")
    public Operation updateDocument(@DestinationVariable String documentId, Operation operation) {
        System.out.println("[Server] : Got a UpdateRequest");
        CRDTService sessionCRDT = documentSessionService.getDocumentSession(documentId).getDocCRDT();
        System.out.println("[Server] : Found The Document");
        switch (operation.getType()) {
            case INSERT:
                sessionCRDT.insertAtCursor(operation.getValue(), operation.getUserId(), operation.getClock());
                break;
            case DELETE:
                sessionCRDT.delete(operation.getNodeId());
                break;
            case UNDO:
                sessionCRDT.undo(operation.getUserId());
                break;
            case REDO:
                sessionCRDT.redo(operation.getUserId());
                break;
            case PASTE:
                sessionCRDT.paste("", documentId, operation.getClock());
                break;
            case COMMENT:
                // TODO: Add CRDT Comment Function Here
                break;
            default:
                System.out.println("[Server] Operation Type is Not Found");
                break;
        }
        System.out.println("[Server] : Updated Now Giving Operations To Other Clients");
        return operation;
    }

    @MessageMapping("/cursorUpdate/{documentId}")
    @SendTo("/topic/Document/{documentId}/cursors")
    public Operation updateCursor(@DestinationVariable String documentId, Operation operation) {
        documentSessionService.getDocumentSession(documentId).getDocCRDT().updateCursor(operation.getUserId(), operation.getNodeId());
        return operation;
    }

    // TODO : Complete On Getting Bondok Get All Cursors
    // @MessageMapping("/getCursors/{documentId}")
    // @SendTo("/topic/Document/{documentId}/cursors/init")
    // public Map<String, String> sendAllCursors(@DestinationVariable String documentId) {
    //     return documentSessionService.getDocumentSession(documentId).getDocCRDT().
    // }

    @MessageMapping("/getDocument/{documentId}")
    @SendTo("/topic/Document/{documentId}/full")
    public String getFullDocument(@DestinationVariable String documentId) {
        return documentSessionService.getDocumentSession(documentId).getDocCRDT().getDocument();
    }
}
