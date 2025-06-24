package com.cashflow;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestTextField extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #0f0f23;");

        Label title = new Label("TextField Test");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Simple TextField
        TextField simpleField = new TextField();
        simpleField.setPromptText("Type here...");
        simpleField.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-font-size: 14px;"
        );

        // Custom styled TextField
        TextField customField = new TextField();
        customField.setPromptText("Custom styled field");
        customField.setStyle(
            "-fx-background-color: #2a3441;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #64748b;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8px;"
        );

        // Test button
        Button testButton = new Button("Print Values");
        testButton.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;"
        );
        testButton.setOnAction(e -> {
            System.out.println("Simple field: '" + simpleField.getText() + "'");
            System.out.println("Custom field: '" + customField.getText() + "'");
        });

        root.getChildren().addAll(title, simpleField, customField, testButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("TextField Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 