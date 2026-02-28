-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 28, 2026 at 06:09 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pidev`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `ajouter_ligne_commande` (IN `p_id_commande` INT, IN `p_type_produit` VARCHAR(10), IN `p_id_item` INT, IN `p_nom_item` VARCHAR(200), IN `p_prix_unitaire` DECIMAL(10,2), IN `p_quantite` INT, IN `p_details` VARCHAR(255), IN `p_image_url` VARCHAR(500))   BEGIN
    DECLARE v_sous_total DECIMAL(10, 2);
    SET v_sous_total = p_prix_unitaire * p_quantite;
    
    INSERT INTO lignes_commande (id_commande, type_produit, id_item, nom_item,
                                 prix_unitaire, quantite, sous_total, 
                                 details, image_url)
    VALUES (p_id_commande, p_type_produit, p_id_item, p_nom_item,
            p_prix_unitaire, p_quantite, v_sous_total,
            p_details, p_image_url);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `changer_statut_commande` (IN `p_id_commande` INT, IN `p_nouveau_statut` VARCHAR(20))   BEGIN
    UPDATE commandes 
    SET statut = p_nouveau_statut 
    WHERE id_commande = p_id_commande;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `creer_commande` (IN `p_nom_client` VARCHAR(100), IN `p_email_client` VARCHAR(100), IN `p_telephone_client` VARCHAR(20), IN `p_adresse_client` TEXT, IN `p_sous_total` DECIMAL(10,2), IN `p_frais_livraison` DECIMAL(10,2), OUT `p_id_commande` INT)   BEGIN
    INSERT INTO commandes (nom_client, email_client, telephone_client, 
                          adresse_client, sous_total, frais_livraison, 
                          total, statut)
    VALUES (p_nom_client, p_email_client, p_telephone_client, 
            p_adresse_client, p_sous_total, p_frais_livraison,
            p_sous_total + p_frais_livraison, 'EN_ATTENTE');
    
    SET p_id_commande = LAST_INSERT_ID();
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `alertes_stock`
--

CREATE TABLE `alertes_stock` (
  `id_alerte` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `type_produit` enum('PRODUIT','KIT') NOT NULL,
  `nom_produit` varchar(255) NOT NULL,
  `stock_actuel` int(11) NOT NULL,
  `seuil_alerte` int(11) DEFAULT 5,
  `date_alerte` datetime DEFAULT current_timestamp(),
  `vue` tinyint(1) DEFAULT 0,
  `resolue` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commandes`
--

CREATE TABLE `commandes` (
  `id_commande` int(11) NOT NULL,
  `nom_client` varchar(100) NOT NULL,
  `email_client` varchar(100) NOT NULL,
  `telephone_client` varchar(20) NOT NULL,
  `adresse_client` text NOT NULL,
  `sous_total` decimal(10,2) NOT NULL,
  `frais_livraison` decimal(10,2) NOT NULL DEFAULT 7.00,
  `total` decimal(10,2) NOT NULL,
  `date_commande` datetime NOT NULL DEFAULT current_timestamp(),
  `statut` enum('EN_ATTENTE','CONFIRMEE','EXPEDIEE','LIVREE','ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `commandes`
--

INSERT INTO `commandes` (`id_commande`, `nom_client`, `email_client`, `telephone_client`, `adresse_client`, `sous_total`, `frais_livraison`, `total`, `date_commande`, `statut`) VALUES
(1, 'Ahmed Ben Ali', 'ahmed@example.com', '+216 20 123 456', '15 Avenue Habib Bourguiba, Tunis 1000', 125.50, 7.00, 132.50, '2026-02-18 04:02:59', 'CONFIRMEE'),
(2, 'imen bouabdallah', 'im@lammetna.tn', '56107714', 'tunisie ariana 2083', 80.00, 7.00, 87.00, '2026-02-18 04:19:51', 'CONFIRMEE'),
(3, 'haha', 'in@exmop.tn', '56525844', 'ariana 2083', 165.00, 7.00, 172.00, '2026-02-18 12:04:12', 'CONFIRMEE'),
(4, 'imen', 'imen.bouabdallah@esprit.tn', '56107714', 'ariana 2083', 78.00, 7.00, 85.00, '2026-02-18 16:14:37', 'EXPEDIEE'),
(5, 'yasmine', 'yasmine@esprit.tn', '465698797', 'ariana 2083', 128.00, 7.00, 135.00, '2026-02-18 17:49:50', 'LIVREE'),
(6, 'iii', 'imen@ze.tn', '45124512', 'iiiizk', 241.00, 7.00, 248.00, '2026-02-24 09:59:35', 'CONFIRMEE'),
(7, 'imene bouabdallah', 'imen.bouabdallah@esprit.tn', '56107714', 'ariana 2083', 78.00, 7.00, 85.00, '2026-02-24 10:14:01', 'CONFIRMEE'),
(8, 'immme', 'imen.bouabdallah@esprit.tn', '+21656107714', 'gggggg', 35.00, 7.00, 42.00, '2026-02-24 10:14:59', 'CONFIRMEE'),
(9, 'imen', 'imm@esprit.tn', '+21656107714', 'ariana 2083', 103.00, 7.00, 110.00, '2026-02-24 10:27:43', 'CONFIRMEE'),
(10, 'noo', 'nooor@rpp.tn', '+21690301540', '45e7', 35.00, 7.00, 42.00, '2026-02-24 10:29:03', 'CONFIRMEE'),
(11, 'imen', 'imen.bouabdallah@esprit.tn', '+21656107714', 'ariana 2083', 78.00, 7.00, 85.00, '2026-02-24 10:36:31', 'CONFIRMEE'),
(12, 'noawar', 'nor@mdini.tn', '+21690301540', 'ariiiiana madina', 25.00, 7.00, 32.00, '2026-02-24 10:38:04', 'CONFIRMEE'),
(13, 'iioa', 'ooioi@oioi.tn', '+21656107714', 'ariana 2083', 25.00, 7.00, 32.00, '2026-02-24 10:46:38', 'CONFIRMEE'),
(14, 'imo', 'io@esprot.tn', '+21656107714', 'arianatunisia', 35.00, 7.00, 42.00, '2026-02-24 11:50:44', 'CONFIRMEE'),
(15, 'ioam', 'iam@esprit..tn', '+21656107714', 'zuia', 120.00, 7.00, 127.00, '2026-02-24 11:54:53', 'CONFIRMEE'),
(16, 'imen', 'imen@esprit.tn', '+21656107714', 'tounisia', 45.00, 7.00, 52.00, '2026-02-28 17:09:56', 'CONFIRMEE');

-- --------------------------------------------------------

--
-- Table structure for table `favoris`
--

CREATE TABLE `favoris` (
  `id_favori` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `type_item` varchar(20) NOT NULL,
  `id_item` int(11) NOT NULL,
  `date_ajout` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `favoris`
--

INSERT INTO `favoris` (`id_favori`, `id_utilisateur`, `type_item`, `id_item`, `date_ajout`) VALUES
(1, 7, 'PRODUIT', 35, '2026-02-28 02:58:02'),
(2, 7, 'KIT', 2, '2026-02-28 02:58:02'),
(3, 7, 'PRODUIT', 43, '2026-02-28 02:58:02'),
(4, 9, 'PRODUIT', 35, '2026-02-28 04:00:57'),
(5, 9, 'PRODUIT', 43, '2026-02-28 04:07:07'),
(7, 9, 'PRODUIT', 45, '2026-02-28 04:07:15'),
(8, 9, 'KIT', 2, '2026-02-28 04:19:41'),
(10, 9, 'PRODUIT', 48, '2026-02-28 04:44:07'),
(11, 10, 'PRODUIT', 48, '2026-02-28 05:00:12'),
(12, 10, 'KIT', 18, '2026-02-28 05:29:41'),
(14, 9, 'PRODUIT', 49, '2026-02-28 16:06:30'),
(17, 9, 'KIT', 1, '2026-02-28 16:07:17');

-- --------------------------------------------------------

--
-- Table structure for table `historique_recommandations`
--

CREATE TABLE `historique_recommandations` (
  `id_historique` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `type_item` varchar(20) NOT NULL,
  `id_item` int(11) NOT NULL,
  `score_pertinence` decimal(5,2) DEFAULT NULL,
  `date_recommandation` timestamp NOT NULL DEFAULT current_timestamp(),
  `a_ete_consulte` tinyint(1) DEFAULT 0,
  `a_ete_achete` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `kit_hobby_artisanal`
--

CREATE TABLE `kit_hobby_artisanal` (
  `id_kit` int(11) NOT NULL,
  `nom_kit` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `type_artisanat` varchar(100) DEFAULT NULL,
  `niveau_difficulte` varchar(50) DEFAULT NULL,
  `prix` decimal(10,2) NOT NULL,
  `stock` int(11) DEFAULT 0,
  `image_url` varchar(255) DEFAULT NULL,
  `id_produit` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `kit_hobby_artisanal`
--

INSERT INTO `kit_hobby_artisanal` (`id_kit`, `nom_kit`, `description`, `type_artisanat`, `niveau_difficulte`, `prix`, `stock`, `image_url`, `id_produit`) VALUES
(1, 'Kit Pottery', 'Kit poterie débutant', 'Poterie', 'Facile', 120.00, 10, 'kit.jpg', 1),
(2, 'Kit Pottery', 'Kit poterie débutant', 'Poterie', 'Facile', 120.00, 10, 'kit.jpg', 1),
(4, 'henna', 'sss', 'Sculpture', 'Difficile', 7874.00, 444, '', 5),
(18, 'zit zitouna', 'a3mel zit zitouna wahdk frais w bnin yhabel', 'Vannerie', 'Difficile', 88.00, 457, '/images/uploads/kit_20260228_062832.jpg', 49);

-- --------------------------------------------------------

--
-- Table structure for table `lignes_commande`
--

CREATE TABLE `lignes_commande` (
  `id_ligne` int(11) NOT NULL,
  `id_commande` int(11) NOT NULL,
  `type_produit` enum('PRODUIT','KIT') NOT NULL,
  `id_item` int(11) NOT NULL,
  `nom_item` varchar(200) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `quantite` int(11) NOT NULL DEFAULT 1,
  `sous_total` decimal(10,2) NOT NULL,
  `details` varchar(255) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `lignes_commande`
--

INSERT INTO `lignes_commande` (`id_ligne`, `id_commande`, `type_produit`, `id_item`, `nom_item`, `prix_unitaire`, `quantite`, `sous_total`, `details`, `image_url`) VALUES
(1, 1, 'PRODUIT', 1, 'Poterie Artisanale', 45.50, 2, 91.00, 'Artisanat • Nabeul', NULL),
(2, 1, 'KIT', 1, 'Kit Poterie Débutant', 34.50, 1, 34.50, 'Poterie • Facile', NULL),
(3, 2, 'PRODUIT', 38, 'Chéchia traditionnelle', 45.00, 1, 45.00, 'Artisanat • Tunis', 'chechia.jpg'),
(4, 2, 'PRODUIT', 33, 'Chéchia traditionnelle', 35.00, 1, 35.00, 'Artisanat • Tunis', 'chechia.jpg'),
(5, 3, 'KIT', 14, 'cigarettes', 45.00, 1, 45.00, 'Calligraphie • Moyen', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSOhk4jxvkRRy6pcdxwxpcXKlD_jBVuoFgYbw&s'),
(6, 3, 'KIT', 2, 'Kit Pottery', 120.00, 1, 120.00, 'Poterie • Facile', 'kit.jpg'),
(7, 4, 'PRODUIT', 45, 'hhhh', 78.00, 1, 78.00, NULL, NULL),
(8, 5, 'PRODUIT', 45, 'hhhh', 78.00, 1, 78.00, NULL, NULL),
(9, 5, 'PRODUIT', 43, 'hachicha', 25.00, 2, 50.00, NULL, NULL),
(10, 6, 'PRODUIT', 45, 'hhhh', 78.00, 2, 156.00, NULL, NULL),
(11, 6, 'PRODUIT', 43, 'hachicha', 25.00, 2, 50.00, NULL, NULL),
(12, 6, 'PRODUIT', 35, 'Chéchia traditionnelle', 35.00, 1, 35.00, NULL, NULL),
(13, 7, 'PRODUIT', 45, 'hhhh', 78.00, 1, 78.00, NULL, NULL),
(14, 8, 'PRODUIT', 35, 'Chéchia traditionnelle', 35.00, 1, 35.00, NULL, NULL),
(15, 9, 'PRODUIT', 45, 'hhhh', 78.00, 1, 78.00, NULL, NULL),
(16, 9, 'PRODUIT', 43, 'hachicha', 25.00, 1, 25.00, NULL, NULL),
(17, 10, 'PRODUIT', 35, 'Chéchia traditionnelle', 35.00, 1, 35.00, NULL, NULL),
(18, 11, 'PRODUIT', 45, 'hhhh', 78.00, 1, 78.00, NULL, NULL),
(19, 12, 'PRODUIT', 43, 'hachicha', 25.00, 1, 25.00, NULL, NULL),
(20, 13, 'PRODUIT', 43, 'hachicha', 25.00, 1, 25.00, NULL, NULL),
(21, 14, 'PRODUIT', 35, 'Chéchia traditionnelle', 35.00, 1, 35.00, NULL, NULL),
(22, 15, 'KIT', 2, 'Kit Pottery', 120.00, 1, 120.00, NULL, NULL),
(23, 16, 'PRODUIT', 49, 'zitoun', 45.00, 1, 45.00, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `mouvements_stock`
--

CREATE TABLE `mouvements_stock` (
  `id_mouvement` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `type_produit` enum('PRODUIT','KIT') NOT NULL,
  `type_mouvement` enum('ENTREE','SORTIE','AJUSTEMENT') NOT NULL,
  `quantite` int(11) NOT NULL,
  `stock_avant` int(11) NOT NULL,
  `stock_apres` int(11) NOT NULL,
  `date_mouvement` datetime DEFAULT current_timestamp(),
  `raison` varchar(255) DEFAULT NULL,
  `id_commande` int(11) DEFAULT NULL,
  `id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `paiements`
--

CREATE TABLE `paiements` (
  `id_paiement` int(11) NOT NULL,
  `id_commande` int(11) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `methode_paiement` varchar(50) NOT NULL DEFAULT 'CARTE_BANCAIRE',
  `numero_carte_masque` varchar(20) DEFAULT NULL,
  `statut_paiement` enum('EN_ATTENTE','REUSSI','ECHOUE','REMBOURSE') NOT NULL DEFAULT 'EN_ATTENTE',
  `date_paiement` datetime NOT NULL DEFAULT current_timestamp(),
  `reference_transaction` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `panier_utilisateur`
--

CREATE TABLE `panier_utilisateur` (
  `id_panier` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `type_produit` varchar(10) NOT NULL,
  `id_item` int(11) NOT NULL,
  `nom` varchar(200) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `quantite` int(11) NOT NULL DEFAULT 1,
  `details` varchar(500) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `date_ajout` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `panier_utilisateur`
--

INSERT INTO `panier_utilisateur` (`id_panier`, `id`, `type_produit`, `id_item`, `nom`, `prix_unitaire`, `quantite`, `details`, `image_url`, `date_ajout`) VALUES
(31, 9, 'PRODUIT', 43, 'hachicha', 25.00, 1, 'Alimentation • Djerba', 'https://', '2026-02-28 03:43:28');

-- --------------------------------------------------------

--
-- Table structure for table `preferences_utilisateur`
--

CREATE TABLE `preferences_utilisateur` (
  `id_preference` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `categories_preferees` text DEFAULT NULL,
  `regions_preferees` text DEFAULT NULL,
  `budget_moyen` decimal(10,2) DEFAULT NULL,
  `derniere_mise_a_jour` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `produit_local`
--

CREATE TABLE `produit_local` (
  `id_produit` int(11) NOT NULL,
  `nom` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) NOT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `region` varchar(100) DEFAULT NULL,
  `stock` int(11) DEFAULT 0,
  `image_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `produit_local`
--

INSERT INTO `produit_local` (`id_produit`, `nom`, `description`, `prix`, `categorie`, `region`, `stock`, `image_url`) VALUES
(1, 'Pot Tunisien', 'Pot artisanal', 55.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(5, 'Tap', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(6, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(7, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(9, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(10, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(11, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(12, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(13, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(16, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(18, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(19, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(20, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(21, 'Tapis', 'Tapis', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(22, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(23, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(24, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(25, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 0, 'img2.jpg'),
(26, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(27, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(28, 'Pot Tunisien', 'Pot artisanal', 50.00, 'Artisanat', 'Nabeul', 10, 'img1.jpg'),
(29, 'Tapis', 'Tapis fait main', 120.00, 'Artisanat', 'Kairouan', 5, 'img2.jpg'),
(30, 'imena', 'djhdhdjhdjudyd', 200.00, '875', 'soussa', 457, 'img78.png'),
(31, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main', 35.00, 'Artisanat', 'Tunis', 15, 'chechia.jpg'),
(32, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main', 35.00, 'Artisanat', 'Tunis', 15, 'chechia.jpg'),
(33, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main', 35.00, 'Artisanat', 'Tunis', 15, 'chechia.jpg'),
(34, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main', 35.00, 'Artisanat', 'Tunis', 15, 'chechia.jpg'),
(35, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main', 35.00, 'Artisanat', 'Tunis', 15, 'https://ayshek.com/wp-content/uploads/2022/09/chechia-rouge-1.jpg'),
(38, 'Chéchia traditionnelle', 'Bonnet traditionnel tunisien fait main we love', 45.00, 'Artisanat', 'Tunis', 0, 'chechia.jpg'),
(43, 'hachicha', 'hachicha premium', 25.00, 'Alimentation', 'Djerba', 77, 'https://'),
(45, 'hhhh', '', 78.00, 'Poterie', 'Sfax', 11, 'https://'),
(48, 'lablabbbeli', 'lablebi bnin', 4500.00, 'Alimentation', 'Kairouan', 45, '/images/uploads/produit_20260228_052024.jpg'),
(49, 'zitoun', 'zitoun akhdher tounsi yhabbel', 45.00, 'Alimentation', 'Kairouan', 78, '/images/uploads/produit_20260228_062719.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `reclamation`
--

CREATE TABLE `reclamation` (
  `id` int(11) NOT NULL,
  `sujet` varchar(200) NOT NULL,
  `description` text NOT NULL,
  `statut` enum('EN_ATTENTE','EN_COURS','RESOLUE','REJETEE') DEFAULT 'EN_ATTENTE',
  `priorite` enum('BASSE','MOYENNE','HAUTE','URGENTE') DEFAULT 'MOYENNE',
  `date_creation` datetime DEFAULT current_timestamp(),
  `date_resolution` datetime DEFAULT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `reponse_admin` text DEFAULT NULL,
  `categorie` varchar(50) DEFAULT 'AUTRE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reclamation`
--

INSERT INTO `reclamation` (`id`, `sujet`, `description`, `statut`, `priorite`, `date_creation`, `date_resolution`, `id_utilisateur`, `reponse_admin`, `categorie`) VALUES
(1, 'Produit endommagé', 'Le produit reçu était cassé à la livraison', 'EN_ATTENTE', 'HAUTE', '2026-02-17 19:26:25', NULL, 7, NULL, 'AUTRE'),
(2, 'Retard de livraison', 'Ma commande est en retard de 2 semaines', 'EN_COURS', 'MOYENNE', '2026-02-17 19:26:25', NULL, 7, NULL, 'AUTRE'),
(3, 'Remboursement', 'Je souhaite être remboursé pour le kit pottery', 'RESOLUE', 'BASSE', '2026-02-17 19:26:25', NULL, 7, NULL, 'AUTRE'),
(4, 'Produit endommagé', 'Le produit reçu était cassé à la livraison', 'EN_ATTENTE', 'HAUTE', '2026-02-17 19:36:14', NULL, 7, NULL, 'SERVICE'),
(5, 'Retard de livraison', 'Ma commande est en retard de 2 semaines', 'EN_COURS', 'MOYENNE', '2026-02-17 19:36:14', NULL, 7, NULL, 'TECHNIQUE'),
(6, 'Remboursement kit', 'Je souhaite être remboursé pour le kit pottery', 'RESOLUE', 'BASSE', '2026-02-17 19:36:14', NULL, 7, NULL, 'SERVICE');

-- --------------------------------------------------------

--
-- Table structure for table `session_admin`
--

CREATE TABLE `session_admin` (
  `id` int(11) NOT NULL,
  `id_admin` int(11) NOT NULL,
  `action` varchar(255) DEFAULT NULL,
  `date_action` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `motDePasse` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `role` enum('USER','HOTE','ADMIN') DEFAULT 'USER',
  `statut` enum('ACTIF','DESACTIVE','EN_ATTENTE') DEFAULT 'EN_ATTENTE',
  `photo` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `dateInscription` datetime DEFAULT current_timestamp(),
  `dernierLogin` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `utilisateur`
--

INSERT INTO `utilisateur` (`id`, `nom`, `prenom`, `email`, `motDePasse`, `telephone`, `adresse`, `role`, `statut`, `photo`, `bio`, `dateInscription`, `dernierLogin`) VALUES
(6, 'Admin', 'Lammetna', 'admin@lammetna.tn', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', NULL, NULL, 'ADMIN', 'ACTIF', NULL, NULL, '2026-02-17 18:56:14', '2026-02-28 17:08:26'),
(7, 'Ben Ali', 'Imen', 'imen@lammetna.tn', '0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90', NULL, NULL, 'USER', 'ACTIF', NULL, NULL, '2026-02-17 18:56:14', NULL),
(8, 'Bouabdallah', 'imen', 'im@lammetna.tn', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', '56107714', NULL, 'USER', 'ACTIF', NULL, NULL, '2026-02-17 21:06:21', '2026-02-18 13:13:41'),
(9, 'imene', 'bouabdallah', 'imen.bouabdallah@esprit.tn', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', '56107714', NULL, 'USER', 'ACTIF', NULL, NULL, '2026-02-18 13:20:18', '2026-02-28 17:04:51'),
(10, 'user', 'user', 'user@lammetna.tn', 'ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f', '56107714', NULL, 'USER', 'ACTIF', NULL, NULL, '2026-02-28 05:56:39', '2026-02-28 17:09:08');

-- --------------------------------------------------------

--
-- Stand-in structure for view `vue_commandes_details`
-- (See below for the actual view)
--
CREATE TABLE `vue_commandes_details` (
`id_commande` int(11)
,`nom_client` varchar(100)
,`email_client` varchar(100)
,`telephone_client` varchar(20)
,`adresse_client` text
,`sous_total` decimal(10,2)
,`frais_livraison` decimal(10,2)
,`total` decimal(10,2)
,`date_commande` datetime
,`statut` enum('EN_ATTENTE','CONFIRMEE','EXPEDIEE','LIVREE','ANNULEE')
,`id_ligne` int(11)
,`type_produit` enum('PRODUIT','KIT')
,`id_item` int(11)
,`nom_item` varchar(200)
,`prix_unitaire` decimal(10,2)
,`quantite` int(11)
,`ligne_sous_total` decimal(10,2)
,`details` varchar(255)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vue_commandes_resume`
-- (See below for the actual view)
--
CREATE TABLE `vue_commandes_resume` (
`id_commande` int(11)
,`nom_client` varchar(100)
,`email_client` varchar(100)
,`telephone_client` varchar(20)
,`total` decimal(10,2)
,`date_commande` datetime
,`statut` enum('EN_ATTENTE','CONFIRMEE','EXPEDIEE','LIVREE','ANNULEE')
,`nombre_articles` bigint(21)
,`quantite_totale` decimal(32,0)
);

-- --------------------------------------------------------

--
-- Structure for view `vue_commandes_details`
--
DROP TABLE IF EXISTS `vue_commandes_details`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vue_commandes_details`  AS SELECT `c`.`id_commande` AS `id_commande`, `c`.`nom_client` AS `nom_client`, `c`.`email_client` AS `email_client`, `c`.`telephone_client` AS `telephone_client`, `c`.`adresse_client` AS `adresse_client`, `c`.`sous_total` AS `sous_total`, `c`.`frais_livraison` AS `frais_livraison`, `c`.`total` AS `total`, `c`.`date_commande` AS `date_commande`, `c`.`statut` AS `statut`, `lc`.`id_ligne` AS `id_ligne`, `lc`.`type_produit` AS `type_produit`, `lc`.`id_item` AS `id_item`, `lc`.`nom_item` AS `nom_item`, `lc`.`prix_unitaire` AS `prix_unitaire`, `lc`.`quantite` AS `quantite`, `lc`.`sous_total` AS `ligne_sous_total`, `lc`.`details` AS `details` FROM (`commandes` `c` left join `lignes_commande` `lc` on(`c`.`id_commande` = `lc`.`id_commande`)) ;

-- --------------------------------------------------------

--
-- Structure for view `vue_commandes_resume`
--
DROP TABLE IF EXISTS `vue_commandes_resume`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vue_commandes_resume`  AS SELECT `c`.`id_commande` AS `id_commande`, `c`.`nom_client` AS `nom_client`, `c`.`email_client` AS `email_client`, `c`.`telephone_client` AS `telephone_client`, `c`.`total` AS `total`, `c`.`date_commande` AS `date_commande`, `c`.`statut` AS `statut`, count(`lc`.`id_ligne`) AS `nombre_articles`, sum(`lc`.`quantite`) AS `quantite_totale` FROM (`commandes` `c` left join `lignes_commande` `lc` on(`c`.`id_commande` = `lc`.`id_commande`)) GROUP BY `c`.`id_commande` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alertes_stock`
--
ALTER TABLE `alertes_stock`
  ADD PRIMARY KEY (`id_alerte`),
  ADD KEY `idx_alerte_produit` (`id_produit`,`type_produit`),
  ADD KEY `idx_alerte_non_vue` (`vue`,`resolue`);

--
-- Indexes for table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id_commande`),
  ADD KEY `idx_email` (`email_client`),
  ADD KEY `idx_date` (`date_commande`),
  ADD KEY `idx_statut` (`statut`);

--
-- Indexes for table `favoris`
--
ALTER TABLE `favoris`
  ADD PRIMARY KEY (`id_favori`),
  ADD UNIQUE KEY `unique_favori` (`id_utilisateur`,`type_item`,`id_item`),
  ADD KEY `idx_favoris_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_favoris_type` (`type_item`,`id_item`);

--
-- Indexes for table `historique_recommandations`
--
ALTER TABLE `historique_recommandations`
  ADD PRIMARY KEY (`id_historique`),
  ADD KEY `idx_recommandations_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `kit_hobby_artisanal`
--
ALTER TABLE `kit_hobby_artisanal`
  ADD PRIMARY KEY (`id_kit`),
  ADD KEY `fk_produit` (`id_produit`);

--
-- Indexes for table `lignes_commande`
--
ALTER TABLE `lignes_commande`
  ADD PRIMARY KEY (`id_ligne`),
  ADD KEY `idx_commande` (`id_commande`),
  ADD KEY `idx_type` (`type_produit`),
  ADD KEY `idx_item` (`id_item`);

--
-- Indexes for table `mouvements_stock`
--
ALTER TABLE `mouvements_stock`
  ADD PRIMARY KEY (`id_mouvement`),
  ADD KEY `idx_mouvement_produit` (`id_produit`,`type_produit`),
  ADD KEY `idx_mouvement_date` (`date_mouvement`),
  ADD KEY `idx_mouvement_commande` (`id_commande`);

--
-- Indexes for table `paiements`
--
ALTER TABLE `paiements`
  ADD PRIMARY KEY (`id_paiement`),
  ADD KEY `idx_commande` (`id_commande`),
  ADD KEY `idx_statut` (`statut_paiement`),
  ADD KEY `idx_date` (`date_paiement`);

--
-- Indexes for table `panier_utilisateur`
--
ALTER TABLE `panier_utilisateur`
  ADD PRIMARY KEY (`id_panier`),
  ADD UNIQUE KEY `unique_item` (`id`,`type_produit`,`id_item`),
  ADD KEY `id` (`id`),
  ADD KEY `idx_type` (`type_produit`);

--
-- Indexes for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  ADD PRIMARY KEY (`id_preference`),
  ADD UNIQUE KEY `id_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `produit_local`
--
ALTER TABLE `produit_local`
  ADD PRIMARY KEY (`id_produit`);

--
-- Indexes for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `session_admin`
--
ALTER TABLE `session_admin`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_admin` (`id_admin`);

--
-- Indexes for table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `alertes_stock`
--
ALTER TABLE `alertes_stock`
  MODIFY `id_alerte` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `favoris`
--
ALTER TABLE `favoris`
  MODIFY `id_favori` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `historique_recommandations`
--
ALTER TABLE `historique_recommandations`
  MODIFY `id_historique` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `kit_hobby_artisanal`
--
ALTER TABLE `kit_hobby_artisanal`
  MODIFY `id_kit` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `lignes_commande`
--
ALTER TABLE `lignes_commande`
  MODIFY `id_ligne` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `mouvements_stock`
--
ALTER TABLE `mouvements_stock`
  MODIFY `id_mouvement` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `paiements`
--
ALTER TABLE `paiements`
  MODIFY `id_paiement` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `panier_utilisateur`
--
ALTER TABLE `panier_utilisateur`
  MODIFY `id_panier` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  MODIFY `id_preference` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `produit_local`
--
ALTER TABLE `produit_local`
  MODIFY `id_produit` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- AUTO_INCREMENT for table `reclamation`
--
ALTER TABLE `reclamation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `session_admin`
--
ALTER TABLE `session_admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `favoris`
--
ALTER TABLE `favoris`
  ADD CONSTRAINT `favoris_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `historique_recommandations`
--
ALTER TABLE `historique_recommandations`
  ADD CONSTRAINT `historique_recommandations_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `kit_hobby_artisanal`
--
ALTER TABLE `kit_hobby_artisanal`
  ADD CONSTRAINT `fk_produit` FOREIGN KEY (`id_produit`) REFERENCES `produit_local` (`id_produit`) ON DELETE CASCADE;

--
-- Constraints for table `lignes_commande`
--
ALTER TABLE `lignes_commande`
  ADD CONSTRAINT `lignes_commande_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commandes` (`id_commande`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `mouvements_stock`
--
ALTER TABLE `mouvements_stock`
  ADD CONSTRAINT `mouvements_stock_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commandes` (`id_commande`) ON DELETE SET NULL;

--
-- Constraints for table `paiements`
--
ALTER TABLE `paiements`
  ADD CONSTRAINT `paiements_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commandes` (`id_commande`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `panier_utilisateur`
--
ALTER TABLE `panier_utilisateur`
  ADD CONSTRAINT `panier_utilisateur_ibfk_1` FOREIGN KEY (`id`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  ADD CONSTRAINT `preferences_utilisateur_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `reclamation`
--
ALTER TABLE `reclamation`
  ADD CONSTRAINT `reclamation_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `session_admin`
--
ALTER TABLE `session_admin`
  ADD CONSTRAINT `session_admin_ibfk_1` FOREIGN KEY (`id_admin`) REFERENCES `utilisateur` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
