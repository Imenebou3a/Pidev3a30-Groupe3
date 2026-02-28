package tn.esprit.services;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Service de notification pour les favoris (style popup comme le panier)
 */
public class FavorisNotificationService {
    
    private static FavorisNotificationService instance;
    private int nombreFavoris = 0;
    
    private FavorisNotificationService() {}
    
    public static FavorisNotificationService getInstance() {
        if (instance == null) {
            instance = new FavorisNotificationService();
        }
        return instance;
    }
    
    /**
     * Afficher une notification d'ajout aux favoris
     */
    public void afficherNotificationAjout(String nomItem, String type) {
        nombreFavoris++;
        afficherPopup("✓ Ajouté aux favoris", nomItem, type, true);
    }
    
    /**
     * Afficher une notification de retrait des favoris
     */
    public void afficherNotificationRetrait(String nomItem) {
        if (nombreFavoris > 0) nombreFavoris--;
        afficherPopup("🗑️ Retiré des favoris", nomItem, "", false);
    }
    
    /**
     * Afficher une notification déjà dans les favoris
     */
    public void afficherNotificationDejaDans(String nomItem) {
        afficherPopup("⚠️ Déjà dans les favoris", nomItem, "", false);
    }
    
    /**
     * Obtenir le nombre de favoris
     */
    public int getNombreFavoris() {
        return nombreFavoris;
    }
    
    /**
     * Définir le nombre de favoris
     */
    public void setNombreFavoris(int nombre) {
        this.nombreFavoris = nombre;
    }
    
    /**
     * Afficher le popup de notification
     */
    private void afficherPopup(String titre, String nomItem, String type, boolean succes) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        
        // Contenu du popup
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setStyle(
            "-fx-background-color: " + (succes ? "#10b981" : "#ef4444") + ";" +
            "-fx-background-radius: 15px;" +
            "-fx-padding: 20px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        // Titre
        Label lblTitre = new Label(titre);
        lblTitre.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        // Nom de l'item
        Label lblNom = new Label(nomItem);
        lblNom.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: white;" +
            "-fx-wrap-text: true;" +
            "-fx-max-width: 250px;"
        );
        
        // Type (si fourni)
        if (!type.isEmpty()) {
            Label lblType = new Label(type);
            lblType.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: rgba(255,255,255,0.8);"
            );
            content.getChildren().addAll(lblTitre, lblNom, lblType);
        } else {
            content.getChildren().addAll(lblTitre, lblNom);
        }
        
        // Badge nombre de favoris (si succès)
        if (succes && nombreFavoris > 0) {
            Label lblBadge = new Label("❤️ " + nombreFavoris + " favoris");
            lblBadge.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: rgba(0,0,0,0.2);" +
                "-fx-background-radius: 10px;" +
                "-fx-padding: 5 15;"
            );
            content.getChildren().add(lblBadge);
        }
        
        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: transparent;");
        
        Scene scene = new Scene(root, 300, 150);
        scene.setFill(null);
        popup.setScene(scene);
        
        // Position en haut à droite
        popup.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() - 320);
        popup.setY(20);
        
        // Animation d'entrée (slide from right)
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), content);
        slideIn.setFromX(400);
        slideIn.setToX(0);
        
        // Animation de sortie (fade out)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), content);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2.5));
        fadeOut.setOnFinished(e -> popup.close());
        
        popup.show();
        slideIn.play();
        fadeOut.play();
    }
}
