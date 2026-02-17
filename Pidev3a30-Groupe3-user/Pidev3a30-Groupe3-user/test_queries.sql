-- ============================================
-- TEST QUERIES FOR PIDEV INTEGRATION
-- ============================================

USE pidev;

-- ============================================
-- 1. VERIFY SCHEMA SETUP
-- ============================================

-- Check all tables exist
SHOW TABLES;

-- Check foreign keys
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'pidev'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Check indexes
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'pidev'
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================
-- 2. VERIFY SAMPLE DATA
-- ============================================

-- Count records in each table
SELECT 
    'utilisateur' AS table_name, COUNT(*) AS count FROM utilisateur
UNION ALL
SELECT 'produit_local', COUNT(*) FROM produit_local
UNION ALL
SELECT 'kit_hobby_artisanal', COUNT(*) FROM kit_hobby_artisanal
UNION ALL
SELECT 'reclamation', COUNT(*) FROM reclamation;

-- View admin account
SELECT id, nom, prenom, email, role, statut 
FROM utilisateur 
WHERE role = 'ADMIN';

-- View all products
SELECT id_produit, nom, categorie, region, prix, stock 
FROM produit_local;

-- View all kits with product names
SELECT 
    k.id_kit,
    k.nom_kit,
    k.type_artisanat,
    k.niveau_difficulte,
    k.prix,
    k.stock,
    p.nom AS produit_nom,
    p.region AS produit_region
FROM kit_hobby_artisanal k
INNER JOIN produit_local p ON k.id_produit = p.id_produit;

-- ============================================
-- 3. TEST FOREIGN KEY CONSTRAINTS
-- ============================================

-- Test 1: Try to insert complaint with invalid user ID (should fail)
-- INSERT INTO reclamation (sujet, description, id_utilisateur) 
-- VALUES ('Test', 'Test description', 99999);
-- Expected: ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails

-- Test 2: Try to insert kit with invalid product ID (should fail)
-- INSERT INTO kit_hobby_artisanal (nom_kit, type_artisanat, niveau_difficulte, prix, stock, id_produit)
-- VALUES ('Test Kit', 'Poterie', 'Facile', 50.00, 10, 99999);
-- Expected: ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails

-- Test 3: Try to delete product that has kits (should fail)
-- DELETE FROM produit_local WHERE id_produit = 1;
-- Expected: ERROR 1451 (23000): Cannot delete or update a parent row: a foreign key constraint fails

-- Test 4: Delete user should cascade to complaints
-- First, create test user and complaint
INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, role, statut)
VALUES ('Test', 'User', 'test@test.com', 'password', '12345678', 'USER', 'ACTIF');

SET @test_user_id = LAST_INSERT_ID();

INSERT INTO reclamation (sujet, description, id_utilisateur)
VALUES ('Test Complaint', 'This is a test', @test_user_id);

-- Verify complaint exists
SELECT COUNT(*) AS complaints_before_delete 
FROM reclamation 
WHERE id_utilisateur = @test_user_id;

-- Delete user (should cascade to complaints)
DELETE FROM utilisateur WHERE id = @test_user_id;

-- Verify complaint was deleted
SELECT COUNT(*) AS complaints_after_delete 
FROM reclamation 
WHERE id_utilisateur = @test_user_id;
-- Expected: 0

-- ============================================
-- 4. TEST VIEWS
-- ============================================

-- View complaints with user details
SELECT * FROM v_reclamations_details;

-- View kits with product details
SELECT * FROM v_kits_avec_produits;

-- View global statistics
SELECT * FROM v_statistiques_globales;

-- ============================================
-- 5. TEST STORED PROCEDURES
-- ============================================

-- Test activate account procedure
-- First create test user
INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, role, statut)
VALUES ('Test', 'Activate', 'activate@test.com', 'password', '12345678', 'USER', 'EN_ATTENTE');

SET @activate_user_id = LAST_INSERT_ID();

-- Check status before
SELECT id, nom, prenom, statut FROM utilisateur WHERE id = @activate_user_id;

-- Activate account
CALL sp_activer_compte(@activate_user_id);

-- Check status after
SELECT id, nom, prenom, statut FROM utilisateur WHERE id = @activate_user_id;
-- Expected: statut = 'ACTIF'

-- Cleanup
DELETE FROM utilisateur WHERE id = @activate_user_id;

-- Test respond to complaint procedure
-- First create test user and complaint
INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, role, statut)
VALUES ('Test', 'Complaint', 'complaint@test.com', 'password', '12345678', 'USER', 'ACTIF');

SET @complaint_user_id = LAST_INSERT_ID();

INSERT INTO reclamation (sujet, description, id_utilisateur)
VALUES ('Test Response', 'Need help', @complaint_user_id);

SET @complaint_id = LAST_INSERT_ID();

-- Check before response
SELECT id, sujet, statut, reponse_admin, date_resolution 
FROM reclamation 
WHERE id = @complaint_id;

-- Add response
CALL sp_repondre_reclamation(@complaint_id, 'We are working on it', 'EN_COURS');

-- Check after response
SELECT id, sujet, statut, reponse_admin, date_resolution 
FROM reclamation 
WHERE id = @complaint_id;
-- Expected: statut = 'EN_COURS', reponse_admin = 'We are working on it'

-- Resolve complaint
CALL sp_repondre_reclamation(@complaint_id, 'Issue resolved', 'RESOLUE');

-- Check after resolution
SELECT id, sujet, statut, reponse_admin, date_resolution 
FROM reclamation 
WHERE id = @complaint_id;
-- Expected: statut = 'RESOLUE', date_resolution IS NOT NULL

-- Cleanup
DELETE FROM utilisateur WHERE id = @complaint_user_id;

-- ============================================
-- 6. STATISTICS QUERIES
-- ============================================

-- User statistics by role
SELECT 
    role,
    COUNT(*) AS total,
    SUM(CASE WHEN statut = 'ACTIF' THEN 1 ELSE 0 END) AS actifs,
    SUM(CASE WHEN statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS en_attente,
    SUM(CASE WHEN statut = 'DESACTIVE' THEN 1 ELSE 0 END) AS desactives
FROM utilisateur
GROUP BY role;

-- Complaint statistics by status
SELECT 
    statut,
    COUNT(*) AS total,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM reclamation), 2) AS percentage
FROM reclamation
GROUP BY statut;

-- Complaint statistics by category
SELECT 
    categorie,
    COUNT(*) AS total,
    AVG(CASE WHEN statut = 'RESOLUE' THEN 1 ELSE 0 END) * 100 AS resolution_rate
FROM reclamation
GROUP BY categorie;

-- Product statistics by category
SELECT 
    categorie,
    COUNT(*) AS total_products,
    SUM(stock) AS total_stock,
    AVG(prix) AS avg_price,
    MIN(prix) AS min_price,
    MAX(prix) AS max_price
FROM produit_local
GROUP BY categorie;

-- Kit statistics by type
SELECT 
    type_artisanat,
    COUNT(*) AS total_kits,
    SUM(stock) AS total_stock,
    AVG(prix) AS avg_price
FROM kit_hobby_artisanal
GROUP BY type_artisanat;

-- Kit statistics by difficulty level
SELECT 
    niveau_difficulte,
    COUNT(*) AS total,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM kit_hobby_artisanal), 2) AS percentage
FROM kit_hobby_artisanal
GROUP BY niveau_difficulte;

-- Products with low stock (< 10)
SELECT 
    id_produit,
    nom,
    categorie,
    stock,
    CASE 
        WHEN stock = 0 THEN 'OUT OF STOCK'
        WHEN stock < 5 THEN 'CRITICAL'
        WHEN stock < 10 THEN 'LOW'
    END AS stock_status
FROM produit_local
WHERE stock < 10
ORDER BY stock ASC;

-- Kits with low stock (< 5)
SELECT 
    id_kit,
    nom_kit,
    type_artisanat,
    stock,
    CASE 
        WHEN stock = 0 THEN 'OUT OF STOCK'
        WHEN stock < 3 THEN 'CRITICAL'
        WHEN stock < 5 THEN 'LOW'
    END AS stock_status
FROM kit_hobby_artisanal
WHERE stock < 5
ORDER BY stock ASC;

-- ============================================
-- 7. INTEGRATION QUERIES
-- ============================================

-- Users with their complaint count
SELECT 
    u.id,
    u.nom,
    u.prenom,
    u.email,
    u.role,
    COUNT(r.id) AS total_complaints,
    SUM(CASE WHEN r.statut = 'EN_ATTENTE' THEN 1 ELSE 0 END) AS pending,
    SUM(CASE WHEN r.statut = 'RESOLUE' THEN 1 ELSE 0 END) AS resolved
FROM utilisateur u
LEFT JOIN reclamation r ON u.id = r.id_utilisateur
GROUP BY u.id, u.nom, u.prenom, u.email, u.role
HAVING total_complaints > 0
ORDER BY total_complaints DESC;

-- Products with their kit count
SELECT 
    p.id_produit,
    p.nom AS produit_nom,
    p.categorie,
    p.region,
    p.stock AS produit_stock,
    COUNT(k.id_kit) AS total_kits,
    SUM(k.stock) AS total_kit_stock
FROM produit_local p
LEFT JOIN kit_hobby_artisanal k ON p.id_produit = k.id_produit
GROUP BY p.id_produit, p.nom, p.categorie, p.region, p.stock
ORDER BY total_kits DESC;

-- Recent complaints (last 30 days)
SELECT 
    r.id,
    r.sujet,
    r.statut,
    r.priorite,
    r.categorie,
    CONCAT(u.prenom, ' ', u.nom) AS utilisateur,
    r.date_creation,
    DATEDIFF(NOW(), r.date_creation) AS days_ago
FROM reclamation r
INNER JOIN utilisateur u ON r.id_utilisateur = u.id
WHERE r.date_creation >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY r.date_creation DESC;

-- Average response time for resolved complaints
SELECT 
    AVG(TIMESTAMPDIFF(HOUR, date_creation, date_resolution)) AS avg_hours_to_resolve,
    MIN(TIMESTAMPDIFF(HOUR, date_creation, date_resolution)) AS min_hours,
    MAX(TIMESTAMPDIFF(HOUR, date_creation, date_resolution)) AS max_hours
FROM reclamation
WHERE statut = 'RESOLUE' AND date_resolution IS NOT NULL;

-- ============================================
-- 8. DATA VALIDATION QUERIES
-- ============================================

-- Check for orphaned complaints (user deleted but complaint remains - should not happen)
SELECT r.*
FROM reclamation r
LEFT JOIN utilisateur u ON r.id_utilisateur = u.id
WHERE u.id IS NULL;
-- Expected: 0 rows (cascade delete should prevent this)

-- Check for orphaned kits (product deleted but kit remains - should not happen)
SELECT k.*
FROM kit_hobby_artisanal k
LEFT JOIN produit_local p ON k.id_produit = p.id_produit
WHERE p.id_produit IS NULL;
-- Expected: 0 rows (restrict delete should prevent this)

-- Check for duplicate emails
SELECT email, COUNT(*) AS count
FROM utilisateur
GROUP BY email
HAVING count > 1;
-- Expected: 0 rows (email is UNIQUE)

-- Check for invalid statuses
SELECT * FROM utilisateur WHERE statut NOT IN ('ACTIF', 'DESACTIVE', 'EN_ATTENTE');
SELECT * FROM reclamation WHERE statut NOT IN ('EN_ATTENTE', 'EN_COURS', 'RESOLUE', 'REJETEE');
-- Expected: 0 rows

-- Check for negative stock
SELECT * FROM produit_local WHERE stock < 0;
SELECT * FROM kit_hobby_artisanal WHERE stock < 0;
-- Expected: 0 rows

-- Check for negative prices
SELECT * FROM produit_local WHERE prix < 0;
SELECT * FROM kit_hobby_artisanal WHERE prix < 0;
-- Expected: 0 rows

-- ============================================
-- 9. PERFORMANCE TESTING QUERIES
-- ============================================

-- Explain query plan for complaint list with user names
EXPLAIN SELECT 
    r.*,
    CONCAT(u.prenom, ' ', u.nom) AS nom_utilisateur
FROM reclamation r
INNER JOIN utilisateur u ON r.id_utilisateur = u.id
ORDER BY r.date_creation DESC;

-- Explain query plan for kit list with product names
EXPLAIN SELECT 
    k.*,
    p.nom AS produit_nom,
    p.region AS produit_region
FROM kit_hobby_artisanal k
INNER JOIN produit_local p ON k.id_produit = p.id_produit
ORDER BY k.nom_kit;

-- ============================================
-- 10. CLEANUP QUERIES (USE WITH CAUTION!)
-- ============================================

-- Delete all test data (CAUTION: This will delete all data except admin)
-- DELETE FROM reclamation WHERE id > 0;
-- DELETE FROM kit_hobby_artisanal WHERE id_kit > 0;
-- DELETE FROM produit_local WHERE id_produit > 0;
-- DELETE FROM utilisateur WHERE role != 'ADMIN';

-- Reset auto-increment counters
-- ALTER TABLE utilisateur AUTO_INCREMENT = 1;
-- ALTER TABLE reclamation AUTO_INCREMENT = 1;
-- ALTER TABLE produit_local AUTO_INCREMENT = 1;
-- ALTER TABLE kit_hobby_artisanal AUTO_INCREMENT = 1;

-- ============================================
-- END OF TEST QUERIES
-- ============================================
