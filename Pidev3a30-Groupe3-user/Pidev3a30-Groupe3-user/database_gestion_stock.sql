-- ============================================
-- GESTION AUTOMATIQUE DU STOCK
-- ============================================

-- Table pour historique des mouvements de stock
CREATE TABLE IF NOT EXISTS mouvements_stock (
    id_mouvement INT PRIMARY KEY AUTO_INCREMENT,
    id_produit INT NOT NULL,
    type_produit ENUM('PRODUIT', 'KIT') NOT NULL,
    type_mouvement ENUM('ENTREE', 'SORTIE', 'AJUSTEMENT') NOT NULL,
    quantite INT NOT NULL,
    stock_avant INT NOT NULL,
    stock_apres INT NOT NULL,
    date_mouvement DATETIME DEFAULT CURRENT_TIMESTAMP,
    raison VARCHAR(255),
    id_commande INT NULL,
    id_utilisateur INT NULL,
    FOREIGN KEY (id_commande) REFERENCES commandes(id_commande) ON DELETE SET NULL
);

-- Index pour améliorer les performances
CREATE INDEX idx_mouvement_produit ON mouvements_stock(id_produit, type_produit);
CREATE INDEX idx_mouvement_date ON mouvements_stock(date_mouvement);
CREATE INDEX idx_mouvement_commande ON mouvements_stock(id_commande);

-- Table pour les alertes de stock
CREATE TABLE IF NOT EXISTS alertes_stock (
    id_alerte INT PRIMARY KEY AUTO_INCREMENT,
    id_produit INT NOT NULL,
    type_produit ENUM('PRODUIT', 'KIT') NOT NULL,
    nom_produit VARCHAR(255) NOT NULL,
    stock_actuel INT NOT NULL,
    seuil_alerte INT DEFAULT 5,
    date_alerte DATETIME DEFAULT CURRENT_TIMESTAMP,
    vue BOOLEAN DEFAULT FALSE,
    resolue BOOLEAN DEFAULT FALSE
);

-- Index pour les alertes
CREATE INDEX idx_alerte_produit ON alertes_stock(id_produit, type_produit);
CREATE INDEX idx_alerte_non_vue ON alertes_stock(vue, resolue);

-- Trigger pour créer automatiquement une alerte quand le stock est faible
DELIMITER //

CREATE TRIGGER after_produit_stock_update
AFTER UPDATE ON produits_locaux
FOR EACH ROW
BEGIN
    IF NEW.stock <= 5 AND NEW.stock != OLD.stock THEN
        -- Vérifier si une alerte non résolue existe déjà
        IF NOT EXISTS (
            SELECT 1 FROM alertes_stock 
            WHERE id_produit = NEW.id_produit 
            AND type_produit = 'PRODUIT' 
            AND resolue = FALSE
        ) THEN
            INSERT INTO alertes_stock (id_produit, type_produit, nom_produit, stock_actuel, seuil_alerte)
            VALUES (NEW.id_produit, 'PRODUIT', NEW.nom, NEW.stock, 5);
        ELSE
            -- Mettre à jour l'alerte existante
            UPDATE alertes_stock 
            SET stock_actuel = NEW.stock, 
                date_alerte = CURRENT_TIMESTAMP,
                vue = FALSE
            WHERE id_produit = NEW.id_produit 
            AND type_produit = 'PRODUIT' 
            AND resolue = FALSE;
        END IF;
    END IF;
    
    -- Résoudre l'alerte si le stock est réapprovisionné
    IF NEW.stock > 5 AND OLD.stock <= 5 THEN
        UPDATE alertes_stock 
        SET resolue = TRUE 
        WHERE id_produit = NEW.id_produit 
        AND type_produit = 'PRODUIT' 
        AND resolue = FALSE;
    END IF;
END//

CREATE TRIGGER after_kit_stock_update
AFTER UPDATE ON kits_hobbies
FOR EACH ROW
BEGIN
    IF NEW.stock <= 5 AND NEW.stock != OLD.stock THEN
        IF NOT EXISTS (
            SELECT 1 FROM alertes_stock 
            WHERE id_produit = NEW.id_kit 
            AND type_produit = 'KIT' 
            AND resolue = FALSE
        ) THEN
            INSERT INTO alertes_stock (id_produit, type_produit, nom_produit, stock_actuel, seuil_alerte)
            VALUES (NEW.id_kit, 'KIT', NEW.nom_kit, NEW.stock, 5);
        ELSE
            UPDATE alertes_stock 
            SET stock_actuel = NEW.stock, 
                date_alerte = CURRENT_TIMESTAMP,
                vue = FALSE
            WHERE id_produit = NEW.id_kit 
            AND type_produit = 'KIT' 
            AND resolue = FALSE;
        END IF;
    END IF;
    
    IF NEW.stock > 5 AND OLD.stock <= 5 THEN
        UPDATE alertes_stock 
        SET resolue = TRUE 
        WHERE id_produit = NEW.id_kit 
        AND type_produit = 'KIT' 
        AND resolue = FALSE;
    END IF;
END//

DELIMITER ;

-- Vue pour faciliter la consultation des mouvements
CREATE OR REPLACE VIEW vue_mouvements_stock AS
SELECT 
    m.id_mouvement,
    m.id_produit,
    m.type_produit,
    CASE 
        WHEN m.type_produit = 'PRODUIT' THEN p.nom
        WHEN m.type_produit = 'KIT' THEN k.nom_kit
    END AS nom_produit,
    m.type_mouvement,
    m.quantite,
    m.stock_avant,
    m.stock_apres,
    m.date_mouvement,
    m.raison,
    m.id_commande,
    c.nom_client
FROM mouvements_stock m
LEFT JOIN produits_locaux p ON m.id_produit = p.id_produit AND m.type_produit = 'PRODUIT'
LEFT JOIN kits_hobbies k ON m.id_produit = k.id_kit AND m.type_produit = 'KIT'
LEFT JOIN commandes c ON m.id_commande = c.id_commande
ORDER BY m.date_mouvement DESC;

-- Vue pour les alertes actives
CREATE OR REPLACE VIEW vue_alertes_actives AS
SELECT 
    a.id_alerte,
    a.id_produit,
    a.type_produit,
    a.nom_produit,
    a.stock_actuel,
    a.seuil_alerte,
    a.date_alerte,
    a.vue,
    CASE 
        WHEN a.type_produit = 'PRODUIT' THEN p.categorie
        WHEN a.type_produit = 'KIT' THEN k.type_artisanat
    END AS categorie
FROM alertes_stock a
LEFT JOIN produits_locaux p ON a.id_produit = p.id_produit AND a.type_produit = 'PRODUIT'
LEFT JOIN kits_hobbies k ON a.id_produit = k.id_kit AND a.type_produit = 'KIT'
WHERE a.resolue = FALSE
ORDER BY a.stock_actuel ASC, a.date_alerte DESC;

-- Données de test (optionnel)
-- INSERT INTO mouvements_stock (id_produit, type_produit, type_mouvement, quantite, stock_avant, stock_apres, raison)
-- VALUES (1, 'PRODUIT', 'ENTREE', 50, 10, 60, 'Réapprovisionnement initial');
