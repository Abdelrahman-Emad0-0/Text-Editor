package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;

public class DocPage {
    private BorderPane root;
    private Main mainApp;
    private boolean isEditor;
    private String editorCode;
    private String viewerCode;
    private TextArea textArea;  // Add a reference to the TextArea

    public DocPage(Main mainApp, boolean isEditor, String editorCode, String viewerCode) {
        this.mainApp = mainApp;
        this.isEditor = isEditor;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;

        root = new BorderPane();

        // Top navbar
        HBox navbar = new HBox(20); // spacing between elements
        navbar.setPadding(new Insets(10));
        navbar.setAlignment(Pos.CENTER); // center all items

        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        Button exportBtn = new Button("Export as Text");

        // Share code section
        HBox codesBox = new HBox(10);
        codesBox.setAlignment(Pos.CENTER);

        if (isEditor) {
            codesBox.getChildren().addAll(
                createCodeBox("Editor Code", editorCode),
                createCodeBox("Viewer Code", viewerCode)
            );
        } else {
            codesBox.getChildren().add(createCodeBox("Viewer Code", viewerCode));
        }

        // Search bar
        HBox searchBar = new HBox(5);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search text...");
        Button findBtn = new Button("Find");
        searchBar.getChildren().addAll(new Label("Search:"), searchField, findBtn);

        navbar.getChildren().addAll(undoBtn, redoBtn, exportBtn, codesBox, searchBar);
        root.setTop(navbar);

        // Center content (editor area)
        VBox centerBox = new VBox(10); // spacing for layout
        centerBox.setPadding(new Insets(10));
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setFillWidth(true);

        // Line numbers area
        TextArea lineNumbers = new TextArea("1");
        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setPrefWidth(35);
        lineNumbers.setStyle(
            "-fx-control-inner-background: #f4f4f4;" +
            "-fx-font-size: 14;" +
            "-fx-border-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;" +
            "-fx-background-color: #f4f4f4;"
        );
        lineNumbers.setFont(Font.font("Courier New", 14));

        // Main editor area
        textArea = new TextArea();  // Now it's a member of the class, so it can be accessed by other methods
        textArea.setFont(Font.font("Arial", 14));
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(80);
        textArea.setStyle("-fx-font-size: 14;");

        if (!isEditor) {
            textArea.setEditable(false);
        }

        // Scroll sync
        textArea.scrollTopProperty().addListener((obs, oldVal, newVal) -> {
            lineNumbers.setScrollTop((double) newVal);
        });

        // Line number sync
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n", -1).length;
            int digits = String.valueOf(lines).length();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= lines; i++) {
                sb.append(String.format("%" + digits + "d", i)).append("\n");
            }
            lineNumbers.setText(sb.toString());
        });

        // Find button action
        findBtn.setOnAction(e -> {
            String text = textArea.getText();
            String query = searchField.getText();
            if (query.isEmpty()) return;

            int index = text.indexOf(query);
            if (index >= 0) {
                textArea.requestFocus();
                textArea.selectRange(index, index + query.length());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Text not found.");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        // Export button action (to save text content to a .txt file)
        exportBtn.setOnAction(e -> {
            // Open a file chooser to select the location and file name
            FileChooser fileChooser = new FileChooser();
            
            // Set the extension filter to only allow .txt files
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            
            // Show the save dialog
            File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());  // Assuming mainApp has a getPrimaryStage() method

            if (file != null) {
                // Ensure the file has a .txt extension
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                // Write the content of the text area to the file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(textArea.getText());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "File saved successfully!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving file: " + ex.getMessage());
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        });


        HBox editorBox = new HBox(lineNumbers, textArea);
        editorBox.setSpacing(0);
        editorBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        // Bind heights
        editorBox.prefHeightProperty().bind(centerBox.heightProperty().multiply(0.9));
        lineNumbers.prefHeightProperty().bind(editorBox.heightProperty());
        textArea.prefHeightProperty().bind(editorBox.heightProperty());

        centerBox.getChildren().add(editorBox);
        root.setCenter(centerBox);
    }

    // Add the setTextAreaContent method
    public void setTextAreaContent(String content) {
        textArea.setText(content);  // Sets the content of the TextArea
    }

    public BorderPane getRoot() {
        return root;
    }

    private VBox createCodeBox(String label, String code) {
        VBox box = new VBox(2);
        Label lbl = new Label(label);
        HBox codeRow = new HBox(2);
        TextField codeField = new TextField(code);
        codeField.setEditable(false);
        codeField.setPrefWidth(80);
        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(code);
            clipboard.setContent(content);
        });
        codeRow.getChildren().addAll(codeField, copyBtn);
        box.getChildren().addAll(lbl, codeRow);
        return box;
    }
}