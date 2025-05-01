package com.editor.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.stereotype.Service;

import com.editor.backend.model.CRDTNode;
import com.editor.backend.model.Cursor;
import com.editor.backend.model.Operation;

@Service
public class CRDTService {

    private final String ROOT_ID = "root";
    private final Map<String, CRDTNode> nodeMap = new HashMap<>();
    private final Map<String, Stack<Operation>> undoStacks = new HashMap<>();
    private final Map<String, Stack<Operation>> redoStacks = new HashMap<>();
    private final Map<String, Cursor> userCursors = new HashMap<>();

    public CRDTService() {
        nodeMap.put(ROOT_ID, new CRDTNode(ROOT_ID, null, '#', false, 0, "system", System.currentTimeMillis()));
    }

    // === INSERT CHARACTER AFTER SPECIFIC PARENT ===
    public void insert(char value, String parentId, String userId, long clock) {
        String id = userId + ":" + clock;
        CRDTNode node = new CRDTNode(id, parentId, value, false, clock, userId, System.currentTimeMillis());
        nodeMap.put(id, node);

        CRDTNode parent = nodeMap.get(parentId);
        if (parent != null) {
            parent.getChildren().add(id);
            sortChildren(parent);
        }

        Operation op = new Operation(Operation.Type.INSERT, id, parentId, value, clock, userId);
        undoStacks.computeIfAbsent(userId, k -> new Stack<>()).push(op);
        redoStacks.computeIfAbsent(userId, k -> new Stack<>());
    }

    // === DELETE CHARACTER (TOMBSTONE) ===
    public void delete(String id) {
        CRDTNode node = nodeMap.get(id);
        if (node != null && !node.isDeleted()) {
            node.setDeleted(true);
            Operation op = new Operation(Operation.Type.DELETE, id, node.getParentId(), node.getValue(), node.getLamportClock(), node.getUserId());
            undoStacks.computeIfAbsent(node.getUserId(), k -> new Stack<>()).push(op);
            redoStacks.computeIfAbsent(node.getUserId(), k -> new Stack<>());
        }
    }

    // === UNDO LAST USER ACTION ===
    public void undo(String userId) {
        Stack<Operation> stack = undoStacks.get(userId);
        if (stack == null || stack.isEmpty()) return;

        Operation op = stack.pop();
        redoStacks.get(userId).push(op);

        if (op.getType() == Operation.Type.INSERT) {
            nodeMap.get(op.getNodeId()).setDeleted(true);
        } else {
            nodeMap.get(op.getNodeId()).setDeleted(false);
        }
    }

    // === REDO LAST UNDO ===
    public void redo(String userId) {
        Stack<Operation> stack = redoStacks.get(userId);
        if (stack == null || stack.isEmpty()) return;

        Operation op = stack.pop();
        undoStacks.get(userId).push(op);

        if (op.getType() == Operation.Type.INSERT) {
            nodeMap.get(op.getNodeId()).setDeleted(false);
        } else {
            nodeMap.get(op.getNodeId()).setDeleted(true);
        }
    }

    // === UPDATE CURSOR FOR USER ===
    public void updateCursor(String userId, String nodeId) {
        if (!nodeMap.containsKey(nodeId)) return;

        List<String> ordered = new ArrayList<>();
        dfsCollectIds(ROOT_ID, ordered);
        int visualIndex = ordered.indexOf(nodeId);

        Cursor cursor = new Cursor(userId, nodeId, visualIndex, System.currentTimeMillis());
        userCursors.put(userId, cursor);
    }

    // === GET CURRENT CURSOR FOR USER ===
    public Cursor getCursor(String userId) {
        return userCursors.get(userId);
    }

    // === INSERT CHARACTER AT CURRENT CURSOR POSITION ===
    public void insertAtCursor(char value, String userId, long clock) {
        String parentId = userCursors.containsKey(userId)
                ? userCursors.get(userId).getNodeId()
                : ROOT_ID;

        insert(value, parentId, userId, clock);
        String newId = userId + ":" + clock;
        updateCursor(userId, newId);
    }

    // === GET CURRENT CURSOR INDEX (VISIBLE POSITION) ===
    public int getCursorIndex(String userId) {
        Cursor c = userCursors.get(userId);
        return (c != null) ? c.getVisualIndex() : -1;
    }

    // === RENDER DOCUMENT STRING ===
    public String getDocument() {
        StringBuilder sb = new StringBuilder();
        dfs(ROOT_ID, sb);
        return sb.toString();
    }

    // === DFS TRAVERSAL TO BUILD FINAL DOCUMENT ===
    private void dfs(String nodeId, StringBuilder sb) {
        CRDTNode node = nodeMap.get(nodeId);

        if (!nodeId.equals(ROOT_ID) && !node.isDeleted()) {
            sb.append(node.getValue());
        }

        for (String childId : node.getChildren()) {
            dfs(childId, sb);
        }
    }

    // === DFS TO COLLECT ORDERED NODE IDs (for cursor index) ===
    private void dfsCollectIds(String nodeId, List<String> ids) {
        CRDTNode node = nodeMap.get(nodeId);

        if (!nodeId.equals(ROOT_ID) && !node.isDeleted()) {
            ids.add(nodeId);
        }

        for (String childId : node.getChildren()) {
            dfsCollectIds(childId, ids);
        }
    }

    // === SORT CHILDREN USING AUTOMERGE STYLE ORDERING ===
    private void sortChildren(CRDTNode parent) {
        parent.getChildren().sort((a, b) -> {
            CRDTNode n1 = nodeMap.get(a);
            CRDTNode n2 = nodeMap.get(b);

            int cmp = Long.compare(n2.getLamportClock(), n1.getLamportClock()); // descending
            return cmp != 0 ? cmp : n1.getUserId().compareTo(n2.getUserId());   // ascending
        });
    }
}
