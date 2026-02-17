package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.DatabaseConnection;

import java.sql.SQLException;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            try {
                DatabaseConnection.getConnection();
                System.out.println("Connected to database 'pidev'.");
            } catch (SQLException e) {
                System.err.println("Cannot connect to database: " + e.getMessage());
                System.err.println("Ensure MySQL is running and database 'pidev' exists.");
                // Still launch the app, but show error
            }

            // Load main FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            if (loader.getLocation() == null) {
                throw new RuntimeException("Cannot find MainView.fxml. Make sure it's in src/main/resources/fxml/");
            }
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Lammetna - Discover Tunisia's Hidden Gems");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void stop() throws Exception {
        DatabaseConnection.closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
