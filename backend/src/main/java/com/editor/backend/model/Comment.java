package com.editor.backend.model;

public class Comment {
    private String id;
    private String userId;
    private String content;
    private String startNodeId;
    private String endNodeId;
    private long timestamp;
    private boolean resolved;

    private int startIndex;       // NEW
    private int endIndex;         // NEW
    private String selectedText;  // NEW

    public Comment(String id, String userId, String content, String startNodeId, String endNodeId, long timestamp,
                   int startIndex, int endIndex, String selectedText) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.timestamp = timestamp;
        this.resolved = false;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.selectedText = selectedText;
    }

    // Existing getters and setters...

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStartNodeId() { return startNodeId; }
    public void setStartNodeId(String startNodeId) { this.startNodeId = startNodeId; }

    public String getEndNodeId() { return endNodeId; }
    public void setEndNodeId(String endNodeId) { this.endNodeId = endNodeId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    // NEW getters
    public int getStartIndex() { return startIndex; }
    public int getEndIndex() { return endIndex; }
    public String getSelectedText() { return selectedText; }
}