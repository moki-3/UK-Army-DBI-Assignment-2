package com.example.dbiukarmy;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MilitaryOrgChartLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label userLabel = new Label("Benutzername:");
        TextField userField = new TextField();
        userField.setPromptText("z. B. postgres");

        Label passLabel = new Label("Passwort:");
        PasswordField passField = new PasswordField();

        Label dbLabel = new Label("Datenbankname:");
        TextField dbField = new TextField();
        dbField.setPromptText("z. B. ukarmy");

        Button connectButton = new Button("Verbinden");
        connectButton.setDefaultButton(true);
        Label statusLabel = new Label();

        connectButton.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText();
            String db = dbField.getText().trim();

            if (user.isEmpty() || pass.isEmpty() || db.isEmpty()) {
                statusLabel.setText("Bitte alle Felder ausfüllen.");
                return;
            }

            // Statische Konfiguration in Hauptanwendung setzen
            MilitaryDBGraph.setDatabaseConfig(user, pass, db);

            try {
                // Haupt-Szene im gleichen Fenster starten
                MilitaryDBGraph mainApp = new MilitaryDBGraph();
                mainApp.start(primaryStage);

            } catch (Exception ex) {
                statusLabel.setText("Fehler beim Start: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(10, userLabel, userField, passLabel, passField, dbLabel, dbField, connectButton, statusLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene loginScene = new Scene(layout, 350, 300);
        primaryStage.setTitle("PostgreSQL Verbindung");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
