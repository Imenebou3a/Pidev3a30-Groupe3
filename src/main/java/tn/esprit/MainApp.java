package tn.esprit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Charger la page de connexion
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root);

        // Charger le CSS approprié pour le login
        scene.getStylesheets().add(getClass().getResource("/css/login-design.css").toExternalForm());

        primaryStage.setTitle("Lammetna - Gestion des Utilisateurs");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public static void changeScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
            Scene scene = new Scene(root);

            // Charger le CSS approprié selon la page
            if (fxmlPath.toLowerCase().contains("login")) {
                scene.getStylesheets().add(MainApp.class.getResource("/css/login-design.css").toExternalForm());
            } else {
                scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
            }

            // Changer la scène
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            // FORCER LE PLEIN ÉCRAN avec Platform.runLater
            Platform.runLater(() -> {
                primaryStage.setMaximized(false); // D'abord désactiver
                primaryStage.setMaximized(true);  // Puis réactiver (force le refresh)
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}