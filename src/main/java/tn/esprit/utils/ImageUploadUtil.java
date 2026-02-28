package tn.esprit.utils;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitaire pour gérer l'upload et le stockage des images
 */
public class ImageUploadUtil {
    
    // Dossier où les images seront stockées
    private static final String UPLOAD_DIR = "src/main/resources/images/uploads/";
    private static final String RELATIVE_PATH = "/images/uploads/";
    
    /**
     * Ouvrir un dialogue pour sélectionner une image
     * @param stage La fenêtre parente
     * @return Le fichier sélectionné ou null
     */
    public static File choisirImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        
        // Filtres pour les types d'images
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("GIF", "*.gif"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        return fileChooser.showOpenDialog(stage);
    }
    
    /**
     * Uploader une image et retourner le chemin relatif
     * @param sourceFile Le fichier source à uploader
     * @param prefix Préfixe pour le nom du fichier (ex: "produit", "kit")
     * @return Le chemin relatif de l'image uploadée
     */
    public static String uploadImage(File sourceFile, String prefix) {
        if (sourceFile == null || !sourceFile.exists()) {
            System.err.println("❌ Fichier source invalide");
            return null;
        }
        
        try {
            // Créer le dossier d'upload s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Dossier d'upload créé: " + UPLOAD_DIR);
            }
            
            // Générer un nom de fichier unique
            String extension = getFileExtension(sourceFile.getName());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = prefix + "_" + timestamp + "." + extension;
            
            // Copier le fichier
            Path destinationPath = uploadPath.resolve(fileName);
            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ Image uploadée: " + fileName);
            
            // Retourner le chemin relatif pour la base de données
            return RELATIVE_PATH + fileName;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'upload: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Uploader une image avec dialogue de sélection
     * @param stage La fenêtre parente
     * @param prefix Préfixe pour le nom du fichier
     * @return Le chemin relatif de l'image uploadée
     */
    public static String uploadImageWithDialog(Stage stage, String prefix) {
        File selectedFile = choisirImage(stage);
        if (selectedFile != null) {
            return uploadImage(selectedFile, prefix);
        }
        return null;
    }
    
    /**
     * Supprimer une image uploadée
     * @param relativePath Le chemin relatif de l'image
     * @return true si supprimé avec succès
     */
    public static boolean deleteImage(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        
        try {
            // Convertir le chemin relatif en chemin absolu
            String fileName = relativePath.replace(RELATIVE_PATH, "");
            Path imagePath = Paths.get(UPLOAD_DIR + fileName);
            
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                System.out.println("✅ Image supprimée: " + fileName);
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Obtenir l'extension d'un fichier
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "jpg"; // Extension par défaut
    }
    
    /**
     * Vérifier si un fichier est une image valide
     */
    public static boolean isValidImage(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String extension = getFileExtension(file.getName());
        return extension.matches("(jpg|jpeg|png|gif)");
    }
    
    /**
     * Obtenir la taille d'un fichier en Mo
     */
    public static double getFileSizeMB(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length() / (1024.0 * 1024.0);
    }
    
    /**
     * Vérifier si la taille du fichier est acceptable (max 5 Mo)
     */
    public static boolean isValidSize(File file) {
        return getFileSizeMB(file) <= 5.0;
    }
    
    /**
     * Valider complètement une image
     */
    public static String validateImage(File file) {
        if (file == null || !file.exists()) {
            return "Aucun fichier sélectionné";
        }
        
        if (!isValidImage(file)) {
            return "Format d'image non supporté. Utilisez JPG, PNG ou GIF";
        }
        
        if (!isValidSize(file)) {
            return "L'image est trop grande (max 5 Mo). Taille actuelle: " + 
                   String.format("%.2f Mo", getFileSizeMB(file));
        }
        
        return null; // Pas d'erreur
    }
}
