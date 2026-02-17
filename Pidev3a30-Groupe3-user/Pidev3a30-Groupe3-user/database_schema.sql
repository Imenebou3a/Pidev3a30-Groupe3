-- ============================================
-- PIDEV - Database Schema with Foreign Keys
-- Integration: User, Reclamation, Produit, Kit
-- ============================================

CREATE DATABASE IF NOT EXISTS pidev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pidev;

-- ============================================
-- TABLE: utilisateur
-- ============================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    motDePasse VARCHAR(255) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    adresse VARCHAR(255),
    role ENUM('USER', 'HOTE', 'ADMIN') DEFAULT 'USER',
    statut ENUM('ACTIF', 'DESACTIVE', 'EN_ATTENTE') DEFAULT 'EN_ATTENTE',
    photo VARCHAR(255),
    bio TEXT,
    dateInscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dernierLogin TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLE: produit_local
-- ============================================
CREATE TABLE IF NOT EXISTS produit_local (
    id_produit INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    description TEXT,
    prix DECIMAL(10,2) NOT NULL,
    categorie VARCHAR(50) NOT NULL,
    region VARCHAR(100) NOT NULL,
    stock INT DEFAULT 0,
    image_url VARCHAR(255),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categorie (categorie),
    INDEX idx_region (region),
    INDEX idx_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLE: kit_hobby_artisanal
-- ============================================
CREATE TABLE IF NOT EXISTS kit_hobby_artisanal (
    id_kit INT AUTO_INCREMENT PRIMARY KEY,
    nom_kit VARCHAR(150) NOT NULL,
    description TEXT,
    type_artisanat VARCHAR(50) NOT NULL,
    niveau_difficulte ENUM('Facile', 'Moyen', 'Difficile') DEFAULT 'Moyen',
    prix DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    image_url VARCHAR(255),
    id_produit INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type_artisanat),
    INDEX idx_niveau (niveau_difficulte),
    INDEX idx_stock (stock),
    CONSTRAINT fk_kit_produit FOREIGN KEY (id_produit) 
        REFERENCES produit_local(id_produit) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLE: reclamation
-- ============================================
CREATE TABLE IF NOT EXISTS reclamation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sujet VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    statut ENUM('EN_ATTENTE', 'EN_COURS', 'RESOLUE', 'REJETEE') DEFAULT 'EN_ATTENTE',
    priorite ENUM('BASSE', 'MOYENNE', 'HAUTE') DEFAULT 'MOYENNE',
    categorie ENUM('TECHNIQUE', 'SERVICE', 'HEBERGEMENT', 'EVENEMENT', 'AUTRE') DEFAULT 'AUTRE',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_resolution TIMESTAMP NULL,
    id_utilisateur INT NOT NULL,
    reponse_admin TEXT,
    INDEX idx_statut (statut),
    INDEX idx_priorite (priorite),
    INDEX idx_categorie (categorie),
    INDEX idx_date_creation (date_creation),
    CONSTRAINT fk_reclamation_user FOREIGN KEY (id_utilisateur) 
        REFERENCES utilisateur(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- SAMPLE DATA - Admin User
-- ============================================
-- Password: admin123 (SHA-256 hashed)
INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, role, statut) 
VALUES ('Admin', 'System', 'admin@lammetna.tn', 
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '12345678', 'ADMIN', 'ACTIF')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================
-- SAMPLE DATA - Products
-- ============================================
INSERT INTO produit_local (nom, description, prix, categorie, region, stock, image_url) VALUES
('Poterie Traditionnelle Nabeul', 'Vase en céramique peint à la main', 45.00, 'Poterie', 'Nabeul', 25, 'https://example.com/poterie1.jpg'),
('Tapis Berbère Kairouan', 'Tapis artisanal en laine pure', 350.00, 'Textile', 'Kairouan', 10, 'https://example.com/tapis1.jpg'),
('Bijoux Argent Djerba', 'Collier en argent avec motifs traditionnels', 120.00, 'Bijoux', 'Djerba', 15, 'https://example.com/bijoux1.jpg'),
('Huile d\'Olive Sfax', 'Huile d\'olive extra vierge 1L', 18.00, 'Alimentation', 'Sfax', 50, 'https://example.com/huile1.jpg'),
('Vannerie Tozeur', 'Panier en palmier tressé', 35.00, 'Artisanat', 'Tozeur', 20, 'https://example.com/vannerie1.jpg')
ON DUPLICATE KEY UPDATE id_produit=id_produit;

-- ============================================
-- SAMPLE DATA - Kits (with FK to products)
-- ============================================
INSERT INTO kit_hobby_artisanal (nom_kit, description, type_artisanat, niveau_difficulte, prix, stock, image_url, id_produit) VALUES
('Kit Poterie Débutant', 'Apprenez la poterie avec ce kit complet incluant argile et outils', 'Poterie', 'Facile', 65.00, 12, 'https://example.com/kit_poterie.jpg', 1),
('Kit Tissage Avancé', 'Kit professionnel pour créer votre propre tapis', 'Tissage', 'Difficile', 180.00, 5, 'https://example.com/kit_tissage.jpg', 2),
('Kit Bijouterie Argent', 'Créez vos propres bijoux en argent', 'Bijouterie', 'Moyen', 95.00, 8, 'https://example.com/kit_bijoux.jpg', 3),
('Kit Vannerie Palmier', 'Apprenez l\'art du tressage de palmier', 'Vannerie', 'Facile', 45.00, 15, 'https://example.com/kit_vannerie.jpg', 5),
('Kit Calligraphie Arabe', 'Kit complet pour la calligraphie traditionnelle', 'Calligraphie', 'Moyen', 55.00, 10, 'https://example.com/kit_calli.jpg', 1)
ON DUPLICATE KEY UPDATE id_kit=id_kit;

-- ============================================
-- VIEWS FOR REPORTING
-- ============================================

-- View: Réclamations avec nom utilisateur
CREATE OR REPLACE VIEW v_reclamations_details AS
SELECT 
    r.id,
    r.sujet,
    r.description,
    r.statut,
    r.priorite,
    r.categorie,
    r.date_creation,
    r.date_resolution,
    r.reponse_admin,
    CONCAT(u.prenom, ' ', u.nom) AS nom_utilisateur,
    u.email AS email_utilisateur
FROM reclamation r
INNER JOIN utilisateur u ON r.id_utilisateur = u.id;

-- View: Kits avec produit associé
CREATE OR REPLACE VIEW v_kits_avec_produits AS
SELECT 
    k.id_kit,
    k.nom_kit,
    k.description AS kit_description,
    k.type_artisanat,
    k.niveau_difficulte,
    k.prix AS kit_prix,
    k.stock AS kit_stock,
    p.id_produit,
    p.nom AS produit_nom,
    p.region AS produit_region,
    p.categorie AS produit_categorie
FROM kit_hobby_artisanal k
INNER JOIN produit_local p ON k.id_produit = p.id_produit;

-- View: Statistiques globales
CREATE OR REPLACE VIEW v_statistiques_globales AS
SELECT 
    (SELECT COUNT(*) FROM utilisateur) AS total_utilisateurs,
    (SELECT COUNT(*) FROM utilisateur WHERE statut = 'ACTIF') AS utilisateurs_actifs,
    (SELECT COUNT(*) FROM utilisateur WHERE statut = 'EN_ATTENTE') AS utilisateurs_en_attente,
    (SELECT COUNT(*) FROM produit_local) AS total_produits,
    (SELECT COUNT(*) FROM produit_local WHERE stock < 10) AS produits_stock_bas,
    (SELECT COUNT(*) FROM kit_hobby_artisanal) AS total_kits,
    (SELECT COUNT(*) FROM kit_hobby_artisanal WHERE stock < 5) AS kits_stock_bas,
    (SELECT COUNT(*) FROM reclamation) AS total_reclamations,
    (SELECT COUNT(*) FROM reclamation WHERE statut = 'EN_ATTENTE') AS reclamations_en_attente,
    (SELECT COUNT(*) FROM reclamation WHERE statut = 'EN_COURS') AS reclamations_en_cours,
    (SELECT COUNT(*) FROM reclamation WHERE statut = 'RESOLUE') AS reclamations_resolues;

-- ============================================
-- STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procédure: Activer un compte utilisateur
CREATE PROCEDURE IF NOT EXISTS sp_activer_compte(IN p_user_id INT)
BEGIN
    UPDATE utilisateur 
    SET statut = 'ACTIF' 
    WHERE id = p_user_id;
END //

-- Procédure: Répondre à une réclamation
CREATE PROCEDURE IF NOT EXISTS sp_repondre_reclamation(
    IN p_reclamation_id INT,
    IN p_reponse TEXT,
    IN p_statut VARCHAR(20)
)
BEGIN
    UPDATE reclamation 
    SET reponse_admin = p_reponse,
        statut = p_statut,
        date_resolution = IF(p_statut = 'RESOLUE', NOW(), NULL)
    WHERE id = p_reclamation_id;
END //

-- Procédure: Vérifier stock produit avant ajout kit
CREATE PROCEDURE IF NOT EXISTS sp_verifier_stock_produit(
    IN p_produit_id INT,
    OUT p_stock_disponible INT
)
BEGIN
    SELECT stock INTO p_stock_disponible
    FROM produit_local
    WHERE id_produit = p_produit_id;
END //

DELIMITER ;

-- ============================================
-- TRIGGERS
-- ============================================

DELIMITER //

-- Trigger: Empêcher suppression produit si kits associés
CREATE TRIGGER IF NOT EXISTS trg_before_delete_produit
BEFORE DELETE ON produit_local
FOR EACH ROW
BEGIN
    DECLARE kit_count INT;
    SELECT COUNT(*) INTO kit_count 
    FROM kit_hobby_artisanal 
    WHERE id_produit = OLD.id_produit;
    
    IF kit_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Impossible de supprimer: des kits sont associés à ce produit';
    END IF;
END //

-- Trigger: Log dernière connexion
CREATE TRIGGER IF NOT EXISTS trg_after_update_user_login
AFTER UPDATE ON utilisateur
FOR EACH ROW
BEGIN
    IF NEW.dernierLogin IS NOT NULL AND (OLD.dernierLogin IS NULL OR NEW.dernierLogin > OLD.dernierLogin) THEN
        -- Log peut être ajouté ici si table de logs existe
        SET @last_login = NEW.dernierLogin;
    END IF;
END //

DELIMITER ;

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Indexes supplémentaires pour optimisation
CREATE INDEX IF NOT EXISTS idx_user_role_statut ON utilisateur(role, statut);
CREATE INDEX IF NOT EXISTS idx_reclamation_user_statut ON reclamation(id_utilisateur, statut);
CREATE INDEX IF NOT EXISTS idx_kit_produit ON kit_hobby_artisanal(id_produit);
CREATE INDEX IF NOT EXISTS idx_produit_stock_categorie ON produit_local(stock, categorie);

-- ============================================
-- GRANTS (à adapter selon vos besoins)
-- ============================================

-- GRANT ALL PRIVILEGES ON pidev.* TO 'pidev_user'@'localhost' IDENTIFIED BY 'password';
-- FLUSH PRIVILEGES;

-- ============================================
-- END OF SCHEMA
-- ============================================
