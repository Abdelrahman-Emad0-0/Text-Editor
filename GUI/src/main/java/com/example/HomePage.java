package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class HomePage {
    private VBox root;
    private Main mainApp;

    public HomePage(Main mainApp) {
        this.mainApp = mainApp;
        root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Collaborative Text Editor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button createBtn = new Button("Create New Document");
        createBtn.setOnAction(e -> {
            // Generate random share codes for demonstration
            mainApp.showDocPage(true, generateShareCode(), generateShareCode());
        });

        Button importBtn = new Button("Import Document");
        importBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            
            // Show the file chooser dialog
            File selectedFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

            // If a file is selected, process it
            if (selectedFile != null) {
                try {
                    // Read the content of the file using the Main class's readFile method
                    String fileContent = mainApp.readFile(selectedFile);
                    
                    // Navigate to the DocPage after importing the file
                    mainApp.showDocPage(true, generateShareCode(), generateShareCode());
                    
                    // After navigating to DocPage, set the content in the TextArea
                    DocPage docPage = mainApp.getDocPage(); // Get the current DocPage
                    docPage.setTextAreaContent(fileContent); // Set the file content in the editor

                } catch (IOException ex) {
                    // Handle any errors while reading the file
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error reading file: " + ex.getMessage());
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        });

        HBox joinBox = new HBox(10);
        TextField codeField = new TextField();
        codeField.setPromptText("Enter Share Code");
        Button joinBtn = new Button("Join");
        joinBtn.setOnAction(e -> {
            // For demo, assume code determines role
            boolean isEditor = codeField.getText().startsWith("E");
            mainApp.showDocPage(isEditor, generateShareCode(), generateShareCode());
        });
        joinBox.getChildren().addAll(codeField, joinBtn);
        joinBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, createBtn, importBtn, new Label("Or join with a share code:"), joinBox);
    }

    public VBox getRoot() {
        return root;
    }

    private String generateShareCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}