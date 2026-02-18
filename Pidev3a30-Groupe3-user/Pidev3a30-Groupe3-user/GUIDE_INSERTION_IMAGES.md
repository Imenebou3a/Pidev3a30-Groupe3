# Guide Complet - Insertion d'Images dans JavaFX

## 📋 Table des Matières
1. [Images Locales (dans le projet)](#1-images-locales)
2. [Images depuis URL (https://)](#2-images-depuis-url)
3. [Images depuis la Base de Données](#3-images-depuis-base-de-données)
4. [Upload d'Images](#4-upload-dimages)

---

## 1. Images Locales (dans le projet)

### A. Structure des Dossiers

Créez un dossier pour vos images:
```
src/main/resources/
├── images/
│   ├── produits/
│   │   ├── produit1.jpg
│   │   ├── produit2.png
│   │   └── ...
│   ├── kits/
│   │   ├── kit1.jpg
│   │   └── ...
│   └── icons/
│       ├── logo.png
│       └── ...
├── fxml/
├── css/
└── ...
```

### B. Code Java pour Charger une Image Locale

```java
// Méthode 1: Depuis resources
Image image = new Image(getClass().getResourceAsStream("/images/produits/produit1.jpg"));
imageView.setImage(image);

// Méthode 2: Avec chemin complet
Image image = new Image("file:src/main/resources/images/produits/produit1.jpg");
imageView.setImage(image);

// Méthode 3: Avec gestion d'erreur
try {
    Image image = new Image(getClass().getResourceAsStream("/images/produits/produit1.jpg"));
    if (image.isError()) {
        // Image par défaut si erreur
        image = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
    }
    imageView.setImage(image);
} catch (Exception e) {
    System.err.println("Erreur chargement image: " + e.getMessage());
}
```

### C. Dans votre Code Actuel (ProduitBackController)

Le code actuel utilise déjà cette méthode:
```java
try {
    if (produit.getImageUrl() != null && !produit.getImageUrl().isEmpty()) {
        imageView.setImage(new Image(produit.getImageUrl(), true));
    } else {
        // Image par défaut
        imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Pas+d'image", true));
    }
} catch (Exception e) {
    imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Erreur", true));
}
```

---

## 2. Images depuis URL (https://)

### A. Fonctionnement Actuel

Votre code actuel supporte déjà les URLs! Quand vous ajoutez un produit:

1. Dans le champ "URL Image", entrez une URL complète:
   ```
   https://example.com/images/produit.jpg
   https://i.imgur.com/abc123.jpg
   https://cdn.example.com/produit.png
   ```

2. L'image sera chargée automatiquement dans les cartes

### B. Amélioration: Support URL Locale + Externe

Modifiez votre code pour supporter les deux:

```java
private void chargerImage(ImageView imageView, String imageUrl) {
    try {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Image par défaut
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            return;
        }
        
        Image image;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // URL externe
            image = new Image(imageUrl, true); // true = chargement en arrière-plan
        } else {
            // Chemin local
            if (imageUrl.startsWith("/")) {
                // Depuis resources
                image = new Image(getClass().getResourceAsStream(imageUrl));
            } else {
                // Depuis fichier système
                image = new Image("file:" + imageUrl);
            }
        }
        
        imageView.setImage(image);
        
    } catch (Exception e) {
        System.err.println("Erreur chargement image: " + e.getMessage());
        // Image d'erreur
        imageView.setImage(new Image(getClass().getResourceAsStream("/images/error.png")));
    }
}
```

### C. Utilisation dans creerCarteProduit()

```java
private VBox creerCarteProduit(ProduitLocal produit) {
    VBox card = new VBox(10);
    card.getStyleClass().add("produit-card");
    card.setAlignment(Pos.TOP_CENTER);

    // Image
    ImageView imageView = new ImageView();
    imageView.setFitWidth(220);
    imageView.setFitHeight(150);
    imageView.setPreserveRatio(true);
    imageView.getStyleClass().add("produit-image");
    
    // Utiliser la méthode améliorée
    chargerImage(imageView, produit.getImageUrl());
    
    // ... reste du code
}
```

---

## 3. Images depuis la Base de Données

### A. Option 1: Stocker le Chemin (Recommandé)

**Base de données:**
```sql
ALTER TABLE produits_locaux ADD COLUMN image_url VARCHAR(500);
```

**Stockez uniquement le chemin:**
- URL externe: `https://example.com/image.jpg`
- Chemin local: `/images/produits/produit1.jpg`
- Chemin système: `C:/images/produit1.jpg`

**Avantages:**
- Base de données légère
- Facile à gérer
- Rapide

### B. Option 2: Stocker l'Image en BLOB (Non recommandé)

**Base de données:**
```sql
ALTER TABLE produits_locaux ADD COLUMN image_data LONGBLOB;
```

**Code Java pour sauvegarder:**
```java
public void sauvegarderImageEnBD(File imageFile, int idProduit) throws Exception {
    String sql = "UPDATE produits_locaux SET image_data = ? WHERE id_produit = ?";
    PreparedStatement pst = connection.prepareStatement(sql);
    
    FileInputStream fis = new FileInputStream(imageFile);
    pst.setBinaryStream(1, fis, (int) imageFile.length());
    pst.setInt(2, idProduit);
    
    pst.executeUpdate();
    fis.close();
}
```

**Code Java pour charger:**
```java
public Image chargerImageDepuisBD(int idProduit) throws Exception {
    String sql = "SELECT image_data FROM produits_locaux WHERE id_produit = ?";
    PreparedStatement pst = connection.prepareStatement(sql);
    pst.setInt(1, idProduit);
    
    ResultSet rs = pst.executeQuery();
    if (rs.next()) {
        InputStream is = rs.getBinaryStream("image_data");
        return new Image(is);
    }
    return null;
}
```

**⚠️ Inconvénients:**
- Base de données très lourde
- Performances réduites
- Difficile à gérer

---

## 4. Upload d'Images

### A. Ajouter un Bouton "Parcourir" dans le Formulaire

**Modifier produit_back.fxml:**
```xml
<!-- Ligne URL Image avec bouton -->
<Label text="URL Image" GridPane.columnIndex="2" GridPane.rowIndex="2" styleClass="form-label"/>
<HBox spacing="5" GridPane.columnIndex="3" GridPane.rowIndex="2">
    <TextField fx:id="txtAjoutImageUrl" promptText="https://..." styleClass="form-field" HBox.hgrow="ALWAYS"/>
    <Button text="📁 Parcourir" onAction="#parcourirImage" styleClass="btn-secondary"/>
</HBox>
```

### B. Code Java pour Upload

**Dans ProduitBackController.java:**

```java
@FXML
private void parcourirImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Sélectionner une image");
    
    // Filtres d'extension
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
        new FileChooser.ExtensionFilter("PNG", "*.png"),
        new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg")
    );
    
    // Ouvrir le dialogue
    File selectedFile = fileChooser.showOpenDialog(txtAjoutImageUrl.getScene().getWindow());
    
    if (selectedFile != null) {
        // Option 1: Copier l'image dans le projet
        copierImageDansProjet(selectedFile);
        
        // Option 2: Utiliser le chemin absolu
        // txtAjoutImageUrl.setText(selectedFile.getAbsolutePath());
    }
}

private void copierImageDansProjet(File sourceFile) {
    try {
        // Créer le dossier si nécessaire
        File destDir = new File("src/main/resources/images/produits");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        // Générer un nom unique
        String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
        String newFileName = "produit_" + System.currentTimeMillis() + extension;
        File destFile = new File(destDir, newFileName);
        
        // Copier le fichier
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Mettre à jour le champ avec le chemin relatif
        txtAjoutImageUrl.setText("/images/produits/" + newFileName);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText("Image copiée avec succès!");
        alert.showAndWait();
        
    } catch (IOException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText("Erreur lors de la copie: " + e.getMessage());
        alert.showAndWait();
    }
}
```

**Ajouter les imports:**
```java
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
```

---

## 5. Solution Recommandée pour Votre Projet

### A. Structure Recommandée

```
1. Créer un dossier pour les images:
   src/main/resources/images/
   ├── produits/
   ├── kits/
   └── placeholder.png (image par défaut)

2. Dans la base de données:
   - Stocker uniquement le chemin: /images/produits/produit1.jpg
   - OU stocker l'URL: https://example.com/image.jpg

3. Dans le formulaire:
   - Permettre de saisir une URL
   - OU utiliser le bouton "Parcourir" pour uploader
```

### B. Code Complet pour ProduitBackController

```java
// Méthode universelle pour charger les images
private void chargerImage(ImageView imageView, String imageUrl) {
    try {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Image par défaut
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            return;
        }
        
        Image image;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // URL externe
            image = new Image(imageUrl, true);
        } else if (imageUrl.startsWith("/")) {
            // Chemin depuis resources
            image = new Image(getClass().getResourceAsStream(imageUrl));
        } else {
            // Chemin fichier système
            image = new Image("file:" + imageUrl);
        }
        
        // Vérifier si l'image est chargée
        if (image.isError()) {
            throw new Exception("Erreur de chargement");
        }
        
        imageView.setImage(image);
        
    } catch (Exception e) {
        System.err.println("Erreur chargement image: " + e.getMessage());
        // Image d'erreur
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        } catch (Exception ex) {
            // Si même le placeholder échoue, utiliser une URL externe
            imageView.setImage(new Image("https://via.placeholder.com/220x150?text=Erreur", true));
        }
    }
}
```

---

## 6. Exemples d'URLs qui Fonctionnent

### URLs Publiques pour Tests

```
https://picsum.photos/220/150
https://via.placeholder.com/220x150
https://dummyimage.com/220x150/000/fff
https://i.imgur.com/[ID].jpg
https://images.unsplash.com/photo-[ID]
```

### Services d'Hébergement d'Images Gratuits

1. **Imgur** (https://imgur.com)
   - Upload gratuit
   - URL directe: `https://i.imgur.com/abc123.jpg`

2. **ImgBB** (https://imgbb.com)
   - Upload gratuit
   - URL directe disponible

3. **Cloudinary** (https://cloudinary.com)
   - Plan gratuit disponible
   - CDN rapide

---

## 7. Checklist pour Votre Projet

- [ ] Créer le dossier `src/main/resources/images/`
- [ ] Ajouter une image placeholder: `placeholder.png`
- [ ] Modifier le code pour supporter chemins locaux ET URLs
- [ ] Ajouter le bouton "Parcourir" dans le formulaire (optionnel)
- [ ] Tester avec une URL externe
- [ ] Tester avec une image locale
- [ ] Gérer les erreurs de chargement

---

## 8. Résumé Rapide

**Pour utiliser des URLs (https://):**
✅ Votre code actuel fonctionne déjà!
✅ Entrez simplement l'URL complète dans le champ "URL Image"

**Pour utiliser des images locales:**
1. Créez `src/main/resources/images/produits/`
2. Copiez vos images dedans
3. Dans la base de données, stockez: `/images/produits/nom_image.jpg`
4. Le code chargera automatiquement l'image

**Pour uploader des images:**
1. Ajoutez le bouton "Parcourir" dans le FXML
2. Ajoutez la méthode `parcourirImage()` dans le controller
3. L'image sera copiée dans le projet automatiquement
