package com.editor.backend.model;
import java.util.UUID;

public class User {
    private String userId;
    private String displayName;
    private String role; // Editor or Viewer
    
    public User(String Role) {
        this.userId = UUID.randomUUID().toString();
        if (Role.equalsIgnoreCase("editor") || Role.equalsIgnoreCase("viewer")) {
            this.role = Role;
        }
    }
}
