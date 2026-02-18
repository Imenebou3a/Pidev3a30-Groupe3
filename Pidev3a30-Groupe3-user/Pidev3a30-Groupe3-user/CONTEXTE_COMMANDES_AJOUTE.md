# Contexte et Règles - Onglet Commandes

## ✅ Modifications Effectuées

### 1. Interface FXML (commandes_back.fxml)

**Ajout d'une section "Contexte et Règles"** placée entre les filtres et le tableau:

**Contenu:**
- **Titre:** "ℹ️ Contexte et Règles de Gestion"
- **Règles des statuts:**
  - 🟡 **En Attente:** Commande reçue, en attente de confirmation
  - 🟢 **Confirmée:** Commande validée, préparation en cours
  - 🔵 **Expédiée:** Commande envoyée au client
  - ✅ **Livrée:** Commande réceptionnée par le client
  - 🔴 **Annulée:** Commande annulée (remboursement si nécessaire)

- **Astuce:** 💡 Utilisation des boutons 'Détails' et 'Statut'

### 2. Styles CSS (commandes_back.css)

**Nouveaux styles ajoutés:**

```css
.contexte-container {
    - Fond bleu clair (#e8f4f8)
    - Bordure bleue (#3498db)
    - Coins arrondis
    - Padding de 15px
}

.contexte-titre {
    - Taille: 16px
    - Gras
    - Couleur: #2c3e50
}

.regles-box {
    - Padding gauche: 30px (indentation)
}

.puce {
    - Taille: 16px
    - Couleur bleue (#3498db)
    - Gras
}

.regle-label {
    - Taille: 13px
    - Gras
    - Largeur minimale: 90px
}

.regle-texte {
    - Taille: 13px
    - Couleur: #555
}
```

## 🎨 Apparence Visuelle

La section contexte apparaît comme une **boîte d'information bleue claire** avec:
- Une icône ℹ️ en en-tête
- Des puces bleues (•) pour chaque règle
- Un séparateur avant l'astuce
- Une icône 💡 pour l'astuce

## 📍 Position

Le contexte est placé **entre les filtres de recherche et le tableau des commandes**, offrant une vue d'ensemble des règles avant de consulter les données.

## ✅ Compilation

```
[INFO] BUILD SUCCESS
[INFO] Total time:  0.502 s
```

## 🚀 Pour Voir le Résultat

Lancez l'application:
```bash
mvn javafx:run
```

Naviguez vers: **Backoffice → Commandes**

Le contexte s'affiche automatiquement sous les boutons de filtrage.

## 📝 Avantages

1. **Clarté:** Les administrateurs comprennent immédiatement la signification de chaque statut
2. **Formation:** Nouveau personnel formé rapidement
3. **Référence:** Guide toujours visible pendant le travail
4. **UX améliorée:** Interface plus intuitive et professionnelle
