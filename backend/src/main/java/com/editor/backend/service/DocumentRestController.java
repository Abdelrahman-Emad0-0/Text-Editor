package com.editor.backend.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.editor.backend.model.DocumentSession;

@Controller
@RequestMapping("/api/documents")
public class DocumentRestController {
    

    @PostMapping
    public void createDocument() {
        DocumentSession session = new DocumentSession();
        // TODO : Store In A Structure
    }

    @PostMapping("/{documentId}")
    public void importFile () {
        // TODO: Upload File Into A CRDT
    }

    @PostMapping("/{documentId}")
    public void exportFile () {
        // TODO: Send File Version to Download
    }
}
