package com.editor.backend.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

public class DocumentWebSocketController {
    
    @MessageMapping("/updateDocument/{documentId}")
    @SendTo("/topic/Document/{documentId}")
    public void updateScore(@DestinationVariable String documentId) {
        // TODO : Add Document Updated Here
    }
}
