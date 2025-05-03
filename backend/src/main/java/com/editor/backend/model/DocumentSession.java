package com.editor.backend.model;
import java.util.UUID;
import com.editor.backend.service.CRDTService;

public class DocumentSession {
    private String documentId;
    private String editorCode;
    private String viewerCode;
    private CRDTService docCRDT;

    public DocumentSession () {
        this.documentId = UUID.randomUUID().toString();
        this.editorCode = "E-" + UUID.randomUUID().toString().substring(0, 8);
        this.viewerCode = "V-" + UUID.randomUUID().toString().substring(0, 8);
        this.docCRDT = new CRDTService();
    }

    boolean isEditor(String code) {
        return code.equals(this.editorCode);
    }

    boolean isViewer(String code) {
        return code.equals(this.viewerCode);
    }

    String getViewerCode() {
        return this.viewerCode;
    }

    String getEditorCode() {
        return this.editorCode;
    }
}
