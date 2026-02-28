package tn.esprit.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.application.Platform;

import tn.esprit.entities.ProduitLocal;
import tn.esprit.entities.KitHobbies;
import tn.esprit.entities.Recommendation;
import tn.esprit.entities.Favori;
import tn.esprit.services.RecommendationService;
import tn.esprit.services.OpenAIRecommendationService;
import tn.esprit.services.OpenAIRecommendationService.RecommendationIA;
import tn.esprit.services.ProduitLocalService;
import tn.esprit.services.KitHobbiesService;
import tn.esprit.services.FavoriService;
import tn.esprit.services.FavorisNotificationService;
import tn.esprit.utils.SessionManager;

import java.util.List;

public class RecommendationsFrontController {

    @FXML private FlowPane gridRecommandations;
    @FXML private ComboBox<String> comboNombre;
    @FXML private CheckBox checkIA;
    @FXML private Label lblMessage;
    @FXML private VBox boxVide;
    @FXML private VBox boxLoading;
    @FXML private Label lblLoadingDetail;
    @FXML private HBox boxStats;
    @FXML private Label lblNbRecommandations;
    @FXML private Label lblScoreMoyen;
    @FXML private Label lblTempsGeneration;

    private RecommendationService recommendationService;
    private OpenAIRecommendationService openAIService;
    private ProduitLocalService produitService;
    private KitHobbiesService kitService;
    private FavoriService favoriService;
    private int idUtilisateur;

    @FXML
    public void initialize() {
        recommendationService = new RecommendationService();
        openAIService = new OpenAIRecommendationService();
        produitService = new ProduitLocalService();
        kitService = new KitHobbiesService();
        favoriService = new FavoriService();
        
        idUtilisateur = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        
        comboNombre.setValue("10");
        checkIA.setSelected(false);
        
        genererRecommandations();
    }

    @FXML
    private void actualiserRecommandations() {
        genererRecommandations();
    }

    @FXML
    private void genererRecommandations() {
        int limite = Integer.parseInt(comboNombre.getValue());
        boolean utiliserIA = checkIA.isSelected();
        
        afficherLoading(true);
        afficherVide(false);
        afficherStats(false);
        gridRecommandations.getChildren().clear();
        
        new Thread(() -> {
            try {
                long debut = System.currentTimeMillis();
                
                if (utiliserIA) {
                    Platform.runLater(() -> lblLoadingDetail.setText("Consultation de l'IA OpenAI..."));
                    genererAvecIA(limite, debut);
                } else {
                    Platform.runLater(() -> lblLoadingDetail.setText("Analyse de vos préférences..."));
                    genererClassique(limite, debut);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Erreur génération: " + e.getMessage());
                Platform.runLater(() -> {
                    afficherLoading(false);
                    afficherMessage("❌ Erreur lors de la génération", "error");
                });
            }
        }).start();
    }

    private void genererClassique(int limite, long debut) {
        try {
            List<Recommendation> recommendations = recommendationService.getRecommandations(idUtilisateur, limite);
            
            long duree = System.currentTimeMillis() - debut;
            
            Platform.runLater(() -> {
                afficherLoading(false);
                
                if (recommendations.isEmpty()) {
                    afficherVide(true);
                } else {
                    afficherRecommandationsClassiques(recommendations);
                    afficherStatsClassiques(recommendations, duree);
                    afficherMessage("✓ " + recommendations.size() + " recommandations générées", "success");
                }
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                afficherLoading(false);
                afficherMessage("❌ Erreur: " + e.getMessage(), "error");
            });
        }
    }

    private void genererAvecIA(int limite, long debut) {
        try {
            List<RecommendationIA> recommendations = openAIService.genererRecommandationsIA(idUtilisateur, limite);
            
            long duree = System.currentTimeMillis() - debut;
            
            Platform.runLater(() -> {
                afficherLoading(false);
                
                if (recommendations.isEmpty()) {
                    afficherVide(true);
                } else {
                    afficherRecommandationsIA(recommendations);
                    afficherStatsIA(recommendations, duree);
                    afficherMessage("✓ " + recommendations.size() + " recommandations IA générées", "success");
                }
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                afficherLoading(false);
                afficherMessage("❌ Erreur IA: " + e.getMessage(), "error");
            });
        }
    }

    private void afficherRecommandationsClassiques(List<Recommendation> recommendations) {
        gridRecommandations.getChildren().clear();
        
        for (Recommendation rec : recommendations) {
            VBox carte = creerCarteRecommandationClassique(rec);
            if (carte != null) {
                gridRecommandations.getChildren().add(carte);
            }
        }
    }

    private void afficherRecommandationsIA(List<RecommendationIA> recommendations) {
        gridRecommandations.getChildren().clear();
        
        for (RecommendationIA rec : recommendations) {
            VBox carte = creerCarteRecommandationIA(rec);
            if (carte != null) {
                gridRecommandations.getChildren().add(carte);
            }
        }
    }

    private VBox creerCarteRecommandationClassique(Recommendation rec) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recommendation-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(240);

        try {
            if ("produit".equals(rec.getTypeRecommandation())) {
                ProduitLocal produit = produitService.getById(rec.getIdItem());
                if (produit != null) {
                    return creerCarteProduitRec(produit, (int)rec.getScorePertinence());
                }
            } else if ("kit".equals(rec.getTypeRecommandation())) {
                KitHobbies kit = kitService.getById(rec.getIdItem());
                if (kit != null) {
                    return creerCarteKitRec(kit, (int)rec.getScorePertinence());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur création carte: " + e.getMessage());
        }
        
        return null;
    }

    private VBox creerCarteRecommandationIA(RecommendationIA rec) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recommendation-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(240);

        Label lblIA = new Label("✨ IA");
        lblIA.getStyleClass().add("ia-badge-card");

        Label lblNom = new Label(rec.getNom());
        lblNom.getStyleClass().add("rec-nom");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(220);

        Label lblInfo = new Label(rec.getType().toUpperCase() + " • " + rec.getCategorie());
        lblInfo.getStyleClass().add("rec-info");

        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblScore = new Label("Pertinence: " + rec.getScore() + "%");
        lblScore.getStyleClass().add("rec-score");
        
        ProgressBar progressBar = new ProgressBar(rec.getScore() / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("score-bar");
        
        scoreBox.getChildren().addAll(lblScore, progressBar);

        Label lblRaison = new Label("💡 " + rec.getRaison());
        lblRaison.getStyleClass().add("rec-raison");
        lblRaison.setWrapText(true);
        lblRaison.setMaxWidth(220);

        Button btnVoir = new Button("👁️ Voir détails");
        btnVoir.getStyleClass().add("btn-voir-rec");

        card.getChildren().addAll(lblIA, lblNom, lblInfo, scoreBox, lblRaison, btnVoir);
        return card;
    }

    private VBox creerCarteProduitRec(ProduitLocal produit, int score) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recommendation-card");
        card.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        try {
            if (produit.getImageUrl() != null && !produit.getImageUrl().isEmpty()) {
                imageView.setImage(new Image(produit.getImageUrl(), true));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Produit", true));
        }

        Label lblScore = new Label(score + "%");
        lblScore.getStyleClass().add("score-badge");

        Label lblNom = new Label(produit.getNom());
        lblNom.getStyleClass().add("rec-nom");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(220);

        Label lblInfo = new Label(produit.getCategorie() + " • " + produit.getRegion());
        lblInfo.getStyleClass().add("rec-info");

        Label lblPrix = new Label(String.format("%.2f TND", produit.getPrix()));
        lblPrix.getStyleClass().add("rec-prix");

        Button btnAjouter = new Button("❤️ Ajouter aux favoris");
        btnAjouter.getStyleClass().add("btn-add-fav");
        btnAjouter.setOnAction(e -> ajouterAuxFavoris("PRODUIT", produit.getIdProduit()));

        Button btnVoir = new Button("👁️ Voir");
        btnVoir.getStyleClass().add("btn-voir-rec");

        VBox actions = new VBox(5, btnAjouter, btnVoir);
        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, lblScore, lblNom, lblInfo, lblPrix, actions);
        return card;
    }

    private VBox creerCarteKitRec(KitHobbies kit, int score) {
        VBox card = new VBox(10);
        card.getStyleClass().add("recommendation-card");
        card.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        try {
            if (kit.getImageUrl() != null && !kit.getImageUrl().isEmpty()) {
                imageView.setImage(new Image(kit.getImageUrl(), true));
            }
        } catch (Exception e) {
            imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Kit", true));
        }

        Label lblScore = new Label(score + "%");
        lblScore.getStyleClass().add("score-badge");

        Label lblNom = new Label(kit.getNomKit());
        lblNom.getStyleClass().add("rec-nom");
        lblNom.setWrapText(true);
        lblNom.setMaxWidth(220);

        Label lblInfo = new Label(kit.getTypeArtisanat() + " • " + kit.getNiveauDifficulte());
        lblInfo.getStyleClass().add("rec-info");

        Label lblPrix = new Label(String.format("%.2f TND", kit.getPrix()));
        lblPrix.getStyleClass().add("rec-prix");

        Button btnAjouter = new Button("❤️ Ajouter aux favoris");
        btnAjouter.getStyleClass().add("btn-add-fav");
        btnAjouter.setOnAction(e -> ajouterAuxFavoris("KIT", kit.getIdKit()));

        Button btnVoir = new Button("👁️ Voir");
        btnVoir.getStyleClass().add("btn-voir-rec");

        VBox actions = new VBox(5, btnAjouter, btnVoir);
        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, lblScore, lblNom, lblInfo, lblPrix, actions);
        return card;
    }

    private void afficherStatsClassiques(List<Recommendation> recs, long duree) {
        int scoreMoyen = (int) recs.stream().mapToDouble(Recommendation::getScorePertinence).average().orElse(0);
        
        lblNbRecommandations.setText(String.valueOf(recs.size()));
        lblScoreMoyen.setText(scoreMoyen + "%");
        lblTempsGeneration.setText((duree / 1000.0) + "s");
        
        afficherStats(true);
    }

    private void afficherStatsIA(List<RecommendationIA> recs, long duree) {
        int scoreMoyen = (int) recs.stream().mapToInt(RecommendationIA::getScore).average().orElse(0);
        
        lblNbRecommandations.setText(String.valueOf(recs.size()));
        lblScoreMoyen.setText(scoreMoyen + "%");
        lblTempsGeneration.setText((duree / 1000.0) + "s");
        
        afficherStats(true);
    }

    @FXML
    private void allerFavoris() {
        System.out.println("Navigation vers favoris...");
    }

    private void afficherLoading(boolean visible) {
        boxLoading.setVisible(visible);
        boxLoading.setManaged(visible);
    }

    private void afficherVide(boolean visible) {
        boxVide.setVisible(visible);
        boxVide.setManaged(visible);
    }

    private void afficherStats(boolean visible) {
        boxStats.setVisible(visible);
        boxStats.setManaged(visible);
    }

    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);
        lblMessage.getStyleClass().removeAll("message-success", "message-error");
        lblMessage.getStyleClass().add("message-" + type);
        
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void ajouterAuxFavoris(String typeItem, int idItem) {
        try {
            Favori favori = new Favori();
            favori.setIdUtilisateur(idUtilisateur);
            favori.setTypeItem(typeItem);
            favori.setIdItem(idItem);
            
            boolean success = favoriService.ajouterFavori(favori);
            
            if (success) {
                String nomItem = typeItem.equals("PRODUIT") ? "Produit" : "Kit";
                FavorisNotificationService.getInstance().afficherNotificationAjout(
                    nomItem + " #" + idItem, 
                    "Recommandation"
                );
                afficherMessage("✓ Ajouté aux favoris", "success");
            } else {
                FavorisNotificationService.getInstance().afficherNotificationDejaDans(typeItem + " #" + idItem);
                afficherMessage("⚠️ Déjà dans les favoris", "error");
            }
        } catch (Exception e) {
            afficherMessage("❌ Erreur: " + e.getMessage(), "error");
        }
    }

}
