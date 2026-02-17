-- Database: pidev
-- Run this in MySQL to create the forum tables.

CREATE DATABASE IF NOT EXISTS pidev;
USE pidev;

CREATE TABLE IF NOT EXISTS forum_post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('AVIS', 'RECLAMATION', 'RECOMMANDATION', 'DISCUSSION') NOT NULL,
    related_entity_type VARCHAR(100),
    related_entity_id INT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    rating INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'EN_ATTENTE',
    is_moderated BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    is_archived BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS forum_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_id INT,
    parent_comment_id INT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES forum_comment(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS forum_reaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NULL,
    comment_id INT NULL,
    type VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (post_id IS NOT NULL OR comment_id IS NOT NULL),
    FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES forum_comment(id) ON DELETE CASCADE
);
