package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DocPage {
    private BorderPane root;
    private Main mainApp;
    private boolean isEditor;
    private String editorCode;
    private String viewerCode;
    private TextArea textArea;
    private TextArea lineNumbers;

    public DocPage(Main mainApp, boolean isEditor, String editorCode, String viewerCode) {
        this.mainApp = mainApp;
        this.isEditor = isEditor;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;

        root = new BorderPane();
        
        HBox navbar = createNavbar();
        root.setTop(navbar);
        
        VBox centerBox = createEditorArea();
        root.setCenter(centerBox);
    }

    private HBox createNavbar() {
        HBox navbar = new HBox(20);
        navbar.setPadding(new Insets(10));
        navbar.setAlignment(Pos.CENTER);

        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        Button exportBtn = new Button("Export as Text");

        HBox codesBox = new HBox(10);
        codesBox.setAlignment(Pos.CENTER);
        if (isEditor) {
            codesBox.getChildren().addAll(createCodeBox("Editor Code", editorCode), createCodeBox("Viewer Code", viewerCode));
        } else {
            codesBox.getChildren().add(createCodeBox("Viewer Code", viewerCode));
        }

        HBox searchBar = new HBox(5);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search text...");
        Button findBtn = new Button("Find");

        findBtn.setOnAction(e -> findText(searchField.getText()));

        searchBar.getChildren().addAll(new Label("Search:"), searchField, findBtn);
        navbar.getChildren().addAll(undoBtn, redoBtn, exportBtn, codesBox, searchBar);
        
        exportBtn.setOnAction(e -> exportText());

        return navbar;
    }

    private VBox createEditorArea() {
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setFillWidth(true);

        lineNumbers = new TextArea("1");
        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setPrefWidth(35);
        lineNumbers.setStyle("-fx-control-inner-background: #f4f4f4; -fx-font-size: 14; -fx-border-color: transparent; -fx-focus-color: transparent;");
        lineNumbers.setFont(Font.font("Courier New", 14));

        textArea = new TextArea();
        textArea.setFont(Font.font("Arial", 14));
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(80);
        textArea.setStyle("-fx-font-size: 14;");
        if (!isEditor) textArea.setEditable(false);

        textArea.scrollTopProperty().addListener((obs, oldVal, newVal) -> lineNumbers.setScrollTop((double) newVal));
        
        textArea.textProperty().addListener((obs, oldText, newText) -> updateLineNumbers(newText));

        textArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> highlightCurrentLine());

        HBox editorBox = new HBox(lineNumbers, textArea);
        editorBox.setSpacing(0);
        editorBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        editorBox.prefHeightProperty().bind(centerBox.heightProperty().multiply(0.9));
        lineNumbers.prefHeightProperty().bind(editorBox.heightProperty());
        textArea.prefHeightProperty().bind(editorBox.heightProperty());

        centerBox.getChildren().add(editorBox);
        return centerBox;
    }

    private void updateLineNumbers(String newText) {
        String[] lines = newText.split("\n", -1); // "-1" ensures empty lines are preserved
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            sb.append(i).append("\n");
        }
        lineNumbers.setText(sb.toString());
        highlightCurrentLine();
    }

    private void highlightCurrentLine() {
        int caretPos = textArea.getCaretPosition();
        String[] lines = textArea.getText().split("\n", -1); // Ensure empty lines are counted

        int currentLine = 1;
        int charCount = 0;
        for (int i = 0; i < lines.length; i++) {
            charCount += lines[i].length() + 1; // +1 for the newline character
            if (caretPos < charCount) {
                currentLine = i + 1;
                break;
            }
        }

        // Update line numbers display
        StringBuilder highlightedNumbers = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            if (i == currentLine) {
                highlightedNumbers.append("▶ ").append(i).append(" ◀\n"); // Highlight current line
            } else {
                highlightedNumbers.append("  ").append(i).append("\n");
            }
        }
        lineNumbers.setText(highlightedNumbers.toString());
    }

    private void findText(String query) {
        if (query.isEmpty()) return;

        String text = textArea.getText();
        int index = text.indexOf(query);

        if (index >= 0) {
            textArea.requestFocus();
            textArea.selectRange(index, index + query.length());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Text not found.");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void exportText() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

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
    public void setTextAreaContent(String text) {
        textArea.setText(text);
    }

}