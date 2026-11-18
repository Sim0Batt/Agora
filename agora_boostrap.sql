-- =========================================
-- AGORA DB bootstrap (MySQL)
-- =========================================

DROP DATABASE IF EXISTS AgoraDB;
CREATE DATABASE AgoraDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE AgoraDB;

CREATE TABLE Notes (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  type VARCHAR(20) NOT NULL,
  length INT NOT NULL,
  content LONGTEXT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_notes_userId (userId)
) ENGINE=InnoDB;

CREATE TABLE LatexNotes (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content LONGTEXT NOT NULL,
  type VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_latexnotes_userId (userId)
) ENGINE=InnoDB;

CREATE TABLE Todos(
  id INT NOT NULL AUTO_INCREMENT,
  title TEXT NOT NULL,
  description LONGTEXT NOT NULL,
  expirationDate VARCHAR(100) NOT NULL,
  type VARCHAR(20) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE songs (
  id VARCHAR(36) NOT NULL,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(100) NOT NULL,
  duration INT NOT NULL,
  path VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE playlists (
  id VARCHAR(36) NOT NULL,
  title VARCHAR(100) NOT NULL,
  count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE playlist_songs (
  song_id VARCHAR(36) NOT NULL,
  playlist_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (song_id, playlist_id)
) ENGINE=InnoDB;
