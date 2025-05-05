package com.editor.backend.model;

public class Operation {
    public enum Type { INSERT, DELETE, UNDO, COMMENT, PASTE, REDO }

    private Type type;
    private String nodeId;
    private String parentId;
    private char value;
    private long clock;
    private String userId;

    public Operation(Type type, String nodeId, String parentId, char value, long clock, String userId) {
        this.type = type;
        this.nodeId = nodeId;
        this.parentId = parentId;
        this.value = value;
        this.clock = clock;
        this.userId = userId;
    }

    public Type getType() { return type; }
    public String getNodeId() { return nodeId; }
    public String getParentId() { return parentId; }
    public char getValue() { return value; }
    public long getClock() { return clock; }
    public String getUserId() { return userId; }
}
