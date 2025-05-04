package com.editor.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.stereotype.Service;

import com.editor.backend.model.CRDTNode;
import com.editor.backend.model.Comment;
import com.editor.backend.model.Cursor;
import com.editor.backend.model.Operation;

@Service
public class CRDTService {

    private final String ROOT_ID = "root";
    private final Map<String, CRDTNode> nodeMap = new HashMap<>();
    private final Map<String, Stack<Operation>> undoStacks = new HashMap<>();
    private final Map<String, Stack<Operation>> redoStacks = new HashMap<>();
    private final Map<String, Cursor> userCursors = new HashMap<>();
    private final Map<String, Comment> commentMap = new HashMap<>();

    public CRDTService() {
        nodeMap.put(ROOT_ID, new CRDTNode(ROOT_ID, null, '#', false, 0, "system", System.currentTimeMillis()));
    }

    public void addComment(Comment comment) {
        commentMap.put(comment.getId(), comment);
    }

    public List<Comment> getAllComments() {
        return new ArrayList<>(commentMap.values());
    }

    public void deleteComment(String id) {
        commentMap.remove(id);
    }

    public void resolveComment(String id) {
        Comment c = commentMap.get(id);
        if (c != null) {
            c.setResolved(true);
        }
    }

    private void removeCommentsRelatedToNode(String deletedNodeId) {
        List<String> ordered = new ArrayList<>();
        dfsCollectIds(ROOT_ID, ordered, true); // include deleted
        int deletedIndex = ordered.indexOf(deletedNodeId);

        if (deletedIndex == -1) return;

        commentMap.values().removeIf(comment -> {
            int start = ordered.indexOf(comment.getStartNodeId());
            int end = ordered.indexOf(comment.getEndNodeId());
            return start != -1 && end != -1 && deletedIndex >= start && deletedIndex <= end;
        });
    }

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

    public void delete(String id) {
        CRDTNode node = nodeMap.get(id);
        if (node != null && !node.isDeleted()) {
            node.setDeleted(true);
            Operation op = new Operation(Operation.Type.DELETE, id, node.getParentId(), node.getValue(), node.getLamportClock(), node.getUserId());
            undoStacks.computeIfAbsent(node.getUserId(), k -> new Stack<>()).push(op);
            redoStacks.computeIfAbsent(node.getUserId(), k -> new Stack<>());
            removeCommentsRelatedToNode(id);
        }
    }

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

    public void updateCursor(String userId, String nodeId) {
        if (!nodeMap.containsKey(nodeId)) return;

        List<String> ordered = new ArrayList<>();
        dfsCollectIds(ROOT_ID, ordered, false);
        int visualIndex = ordered.indexOf(nodeId);

        Cursor cursor = new Cursor(userId, nodeId, visualIndex, System.currentTimeMillis());
        userCursors.put(userId, cursor);
    }

    public Cursor getCursor(String userId) {
        return userCursors.get(userId);
    }

    public void insertAtCursor(char value, String userId, long clock) {
        String parentId = userCursors.containsKey(userId)
                ? userCursors.get(userId).getNodeId()
                : ROOT_ID;

        insert(value, parentId, userId, clock);
        String newId = userId + ":" + clock;
        updateCursor(userId, newId);
    }

    public int getCursorIndex(String userId) {
        Cursor c = userCursors.get(userId);
        return (c != null) ? c.getVisualIndex() : -1;
    }

    public String getDocument() {
        StringBuilder sb = new StringBuilder();
        dfs(ROOT_ID, sb);
        return sb.toString();
    }

    private void dfs(String nodeId, StringBuilder sb) {
        CRDTNode node = nodeMap.get(nodeId);

        if (!nodeId.equals(ROOT_ID) && !node.isDeleted()) {
            sb.append(node.getValue());
        }

        for (String childId : node.getChildren()) {
            dfs(childId, sb);
        }
    }

    private void dfsCollectIds(String nodeId, List<String> ids) {
        dfsCollectIds(nodeId, ids, false);
    }

    private void dfsCollectIds(String nodeId, List<String> ids, boolean includeDeleted) {
        CRDTNode node = nodeMap.get(nodeId);

        if (!nodeId.equals(ROOT_ID) && (includeDeleted || !node.isDeleted())) {
            ids.add(nodeId);
        }

        for (String childId : node.getChildren()) {
            dfsCollectIds(childId, ids, includeDeleted);
        }
    }

    private void sortChildren(CRDTNode parent) {
        parent.getChildren().sort((a, b) -> {
            CRDTNode n1 = nodeMap.get(a);
            CRDTNode n2 = nodeMap.get(b);

            int cmp = Long.compare(n2.getLamportClock(), n1.getLamportClock());
            return cmp != 0 ? cmp : n1.getUserId().compareTo(n2.getUserId());
        });
    }

    public void paste(String text, String userId, long startingClock) {
        String parentId = userCursors.containsKey(userId)
                ? userCursors.get(userId).getNodeId()
                : ROOT_ID;

        for (char c : text.toCharArray()) {
            insert(c, parentId, userId, startingClock++);
            parentId = userId + ":" + (startingClock - 1);
        }

        String newCursorId = userId + ":" + (startingClock - 1);
        updateCursor(userId, newCursorId);
    }

    public String copy(String startNodeId, String endNodeId) {
        List<String> ordered = new ArrayList<>();
        dfsCollectIds(ROOT_ID, ordered, false);

        int start = ordered.indexOf(startNodeId);
        int end = ordered.indexOf(endNodeId);

        if (start == -1 || end == -1 || start > end) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            CRDTNode node = nodeMap.get(ordered.get(i));
            if (node != null && !node.isDeleted()) {
                sb.append(node.getValue());
            }
        }
        return sb.toString();
    }
    public void updateCursorByIndex(String userId, int index) {
        List<String> ordered = new ArrayList<>();
        dfsCollectIds(ROOT_ID, ordered, false); // only visible nodes
    
        if (index < 0 || index >= ordered.size()) {
            updateCursor(userId, ROOT_ID); // fallback
            return;
        }
    
        String nodeId = ordered.get(index);
        updateCursor(userId, nodeId);
    }
}
