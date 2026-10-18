-- =========================================
-- AGORA DB bootstrap (MySQL)
-- =========================================

DROP DATABASE IF EXISTS AgoraDB;
CREATE DATABASE AgoraDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE AgoraDB;

CREATE TABLE Users (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  password VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE Notes (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  type VARCHAR(20) NOT NULL,
  length INT NOT NULL,
  userId INT NOT NULL,
  content LONGTEXT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_notes_userId (userId)
) ENGINE=InnoDB;

CREATE TABLE LatexNotes (
  id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  userId INT NOT NULL,
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