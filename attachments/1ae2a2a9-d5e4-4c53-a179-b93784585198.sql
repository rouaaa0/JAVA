-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mar. 29 avr. 2025 à 21:20
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `edunova`
--

-- --------------------------------------------------------

--
-- Structure de la table `blog`
--

CREATE TABLE `blog` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `created_at_blog` datetime NOT NULL,
  `updated_at_blog` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `blog`
--

INSERT INTO `blog` (`id`, `user_id`, `title`, `description`, `created_at_blog`, `updated_at_blog`) VALUES
(11, 1, 'ffffffffffffffff100', 'ffffffffffffffff', '2025-02-26 00:00:00', '2025-04-18 00:00:00'),
(17, 1, 'arwaaa', 'titiejfubfsdfscvvgv', '2025-02-26 21:52:00', '2025-04-17 19:14:58'),
(25, 1, 'arwaaaaaaaaaaaaaa', 'arwaaaaaaaaaaaaaaaaa', '2025-02-27 15:16:00', '2025-02-27 15:16:00'),
(26, 1, 'arwaaaaaaa', 'fuck fuck fuck', '2025-02-27 15:16:00', '2025-02-27 15:16:00'),
(28, 1, 'arwaaaaaaaaaa', 'fuck fuck fuck 1', '2025-02-27 00:00:00', '2025-02-27 00:00:00'),
(53, NULL, 'j,ffjgj;gji;', ':lgufklkluilyidylidyllicy2005', '2025-04-16 00:00:00', '2025-04-25 00:00:00'),
(54, NULL, 'thnngfynt', 'u,tkujgh,cug,gh,hg,hg,hg,kuykd', '2025-04-11 00:00:00', '2025-04-18 00:00:00'),
(55, NULL, 'roua250', 'fhygcbbvjdnnchyoldnvf jniufjcos  sjvg', '2025-04-21 00:00:00', '2025-04-18 00:00:00');

-- --------------------------------------------------------

--
-- Structure de la table `club`
--

CREATE TABLE `club` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `date_creation` date NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `club`
--

INSERT INTO `club` (`id`, `nom`, `logo`, `type`, `date_creation`, `user_id`) VALUES
(5, 'okneajlknflkjnfeljnaefn', '67b1f667021a4.jpg', 'EVENEMENTIEL', '2025-02-06', 0),
(14, 'MotsoMotso', '67b38ff935900.png', 'SPORTIF', '2025-02-18', 0),
(15, 'Robenpspspspspsps', '67b3906067b8f.png', 'SPORTIF', '2018-06-14', 0),
(18, 'CALINO2025', '67b3972f4a836.png', 'EVENEMENTIEL', '2025-02-19', 0),
(22, 'espritpidev333', '67b473290450f.jpg', 'EDUCATIF', '2025-02-27', 0),
(23, 'sdfghklmùmlkjhgf', '67b481f5bc4c3.png', 'EDUCATIF', '2025-02-20', 0),
(27, 'MotsoMotsooooo', '67b4a78600d2a.jpg', 'SPORTIF', '2025-02-19', 0),
(28, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', '67bc5e61becc3.png', 'CULTUREL', '2025-02-25', 5);

-- --------------------------------------------------------

--
-- Structure de la table `cour`
--

CREATE TABLE `cour` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `duree` varchar(255) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `cour`
--

INSERT INTO `cour` (`id`, `nom`, `description`, `duree`, `user_id`) VALUES
(2, 'kooooooooooooooooooooo', 'oooooooooooooooooooooooo', '444', 0),
(5, 'javaScript', 'javaScriot avencee', '42', 0),
(7, 'zfffffffffffffffffff', 'zffffffffffffffffffffffffff', '7', 0),
(8, 'zeeeeeeeeeeeeeeeeeeeeeeeee', 'zeeeeeeeeeeeee', '25', 0),
(9, 'zdddddddddddddddddddddddddddddddddddddddddd', 'zaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', '25', 0),
(10, 'zxwqscdd', 'deeeeeeeeeeeeee', '8', 0),
(11, 'test test', ':kmlkmlkmkmkmlklmkl', '12', 5),
(12, 'test testfinaleeeeeeeeeeeeeeeee', 'test testfinaleeeeeeeeeeeeeeeee', '12', 5);

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20250221232146', '2025-02-22 00:21:55', 180);

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE `evenement` (
  `id` int(11) NOT NULL,
  `club_id` int(11) DEFAULT NULL,
  `titre` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `prix` double NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `x` double NOT NULL,
  `y` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `evenement`
--

INSERT INTO `evenement` (`id`, `club_id`, `titre`, `description`, `prix`, `date_debut`, `date_fin`, `x`, `y`) VALUES
(22, 15, 'GTAGTAGTAGTAGTA', 'BONJOURCJ', 30, '2025-02-18', '2025-02-25', 70.9, 65),
(23, 15, 'GTASAN	 GTASAN', 'HOW TO BE LIKE CJ', 2000, '2025-02-25', '2025-03-05', 36.81, 10.18888),
(24, 18, 'CALINOlove', 'fghjklm', 150, '2025-02-18', '2025-02-25', 50.5, 50),
(25, 18, 'calinoooooooo', 'BONJOURCalinooooo', 30, '2025-02-18', '2025-02-26', 10, 65),
(26, 18, 'CALINOloveeeeeee', 'BONJOURCal', 2000, '2025-02-26', '2025-02-27', 80, 60),
(29, 14, 'wisssssssssssss', 'BONJOURCalino', 150, '2025-02-18', '2025-02-19', 50.5, 26.7),
(30, 5, 'jhkjhjkssssssssss', 'kjhkjhkjhsssssssssssssssssssssss', 111, '2025-02-03', '2025-02-04', 12, 33);

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `available_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `delivered_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `post`
--

CREATE TABLE `post` (
  `id` int(11) NOT NULL,
  `blog_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `content` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `update_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)',
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `post`
--

INSERT INTO `post` (`id`, `blog_id`, `user_id`, `content`, `image`, `update_at`, `created_at`) VALUES
(8, 7, 1, 'AAAA', '753b87ce-3226-485e-9433-44ca6ff1121d-67bf33f792c0d.jpg', '2025-04-17 23:47:44', '2025-02-26 16:31:00'),
(11, 7, 1, 'hhhhhhhhhhhhhhhhhhhhhhh', '753b87ce-3226-485e-9433-44ca6ff1121d-67bf3432aefed.jpg', '2025-02-26 16:33:00', '2025-02-26 16:32:00'),
(12, 7, 1, 'arwa', '753b87ce-3226-485e-9433-44ca6ff1121d-67bf3950dee35.jpg', '2025-04-18 10:43:51', '2025-02-26 16:54:00'),
(13, 17, 1, 'ghrtghghbdhrthy', '753b87ce-3226-485e-9433-44ca6ff1121d-67bf992ce67f4.jpg', '2025-02-26 23:43:00', '2025-02-26 23:43:00'),
(14, 17, 1, 'hg,jfd,n njyhtdjk,htjjkuthjkuujlutyf,khj,u', 'file:/C:/Users/USER/Downloads/images.jpg', '2024-12-26 00:00:00', '2025-02-26 00:00:00'),
(15, 7, 1, 'jfjhyjfjjhgfj', 'images-67c07f79dba26.jpg', '2025-02-27 16:06:00', '2025-02-27 16:06:00'),
(24, 55, NULL, 'hhhhhhh2025', 'file:/C:/Users/USER/Downloads/Untitled%20(1).png', '2025-04-18 00:00:00', '2025-04-18 00:00:00'),
(25, 53, NULL, 'vreihntpzhntera2001', 'file:/C:/Users/USER/Downloads/images.jpg', '2025-04-24 00:00:00', '2025-04-21 00:00:00'),
(29, 55, NULL, 'ghhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhgrb', 'file:/C:/Users/USER/Downloads/unnamed.jpg', '2025-04-19 00:00:00', '2025-04-25 00:00:00');

-- --------------------------------------------------------

--
-- Structure de la table `seance`
--

CREATE TABLE `seance` (
  `id` int(11) NOT NULL,
  `cour_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `professeur` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `seance`
--

INSERT INTO `seance` (`id`, `cour_id`, `date`, `professeur`) VALUES
(4, 5, '2025-02-18', 'wissem'),
(5, 5, '2025-02-21', 'wissem'),
(6, 2, '2025-03-07', 'klmljlkjkjlkmùmlmlmkl');

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `profilepic` varchar(255) DEFAULT NULL,
  `reset_token` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `name`, `lastname`, `email`, `password`, `role`, `profilepic`, `reset_token`) VALUES
(1, 'souhir', 'souhir', 'souhir@gmail.com', '$2a$10$PY974j00M.zYpi0cOdSi0.0kJQoQ/1Qr6uzODcAxK4qjyeLQzULlG', 'ROLE_ADMIN', 'unnamed-67b90af156421.jpg', NULL),
(7, 'zakaria', 'bensalah', 'zakaria@gmail.com', '$2a$10$HZiB0Af56bLY6RT.PwEze.TdCQxpSr/uQhX1U1BVrxTYVNoBH7/pa', 'ROLE_USER', '753b87ce-3226-485e-9433-44ca6ff1121d-67be2f450bd06.jpg', NULL),
(46, 'adi55', 'aberrahim', 'adi@gmail.com', '$2a$10$5gmnjFjrH85G1fyEXzqT1eiGoVnAn82pJxd/VOWTdCTeItcNSNTra', 'ROLE_USER', 'C:\\Users\\USER\\Downloads\\753b87ce-3226-485e-9433-44ca6ff1121d.jpg', NULL),
(63, 'rayen', 'rayen', 'rayen@gmail.com', '$2a$10$27.PSY5OuWo56zZNqooI9OWeWOZ0ZawhWRWS05t1Z1Jfa4GzGM4lq', 'ROLE_ADMIN', 'C:\\Users\\USER\\Downloads\\462639889_949141450397155_6450600948695273877_n-removebg-preview.png', NULL),
(64, 'monji', 'monji', 'monji@gmail.com', '$2a$10$23Gk7hDkfWy/wYLm/1PqoOnlP2X3hSwVPdP6YSmbGkmfhjAS6pCN6', 'ROLE_USER', 'C:\\Users\\USER\\Downloads\\452362661_1146035999798698_4736248851312892808_n.jpg', NULL),
(69, 'roua', 'roua', 'roua@gmail.com', 'Roua2003', 'ROLE_ADMIN', 'C:\\Users\\USER\\Downloads\\474959700_1275958463614111_5177697323130101185_n.jpg', NULL),
(70, 'saida', 'saaida', 'saida@gmail.com', '$2a$10$8ejMeTBl51c3ZrgX7XyB4u2sK1fsEFhPt3blDKSviFVtBNuZphvFa', 'ROLE_USER', NULL, NULL),
(81, 'salim', 'salim', 'salim@gmail.com', 'Salim2003', 'ROLE_ADMIN', NULL, NULL),
(82, 'test', 'test', 'test@gmail.com', '$2a$10$pXR/iik.t.Kvc3Piza1TT.2VEH.NGk2..UOhmomxxa4FYEvrnwPFO', 'ROLE_USER', 'C:\\Users\\USER\\Downloads\\5c75560a-538f-4ac0-99d4-6cd477178cfd.jpg', NULL),
(83, 'kjhhj', 'jhjhjh', 'jhjhj@jghgh.hjh', '$2a$10$2gL2wyB3dtVRHOXGG/MEX.VAB6j0An9ygQ4SSPLFKD9RrTfanwe9C', 'ROLE_USER', 'C:\\Users\\USER\\OneDrive\\Pictures\\c0ca3b2a142261729723ced4af28638a.jpg', NULL),
(84, 'roua', 'roua', 'rouaabderrahim00@gmail.com', '$2a$10$ODDzweRHgEAOD7hOMBNySeU3JBC4RncFrikOcLuY93ybxt6XRwF3S', 'ROLE_USER', '', NULL),
(85, 'arwa', 'arwa', 'arwaabderrahim1@gmail.com', '$2a$10$2H58IS2hMEPgRp/XjuiAFO8bvyknemzGyDNI.TPG4re0sgBheNUK6', 'ROLE_USER', '', NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `blog`
--
ALTER TABLE `blog`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_C0155143A76ED395` (`user_id`);

--
-- Index pour la table `club`
--
ALTER TABLE `club`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_club_user` (`user_id`);

--
-- Index pour la table `cour`
--
ALTER TABLE `cour`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_cour_user` (`user_id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0` (`queue_name`),
  ADD KEY `IDX_75EA56E0E3BD61CE` (`available_at`),
  ADD KEY `IDX_75EA56E016BA31DB` (`delivered_at`);

--
-- Index pour la table `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_5A8A6C8DDAE07E97` (`blog_id`),
  ADD KEY `IDX_5A8A6C8DA76ED395` (`user_id`);

--
-- Index pour la table `seance`
--
ALTER TABLE `seance`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8D93D649E7927C74` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `blog`
--
ALTER TABLE `blog`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=59;

--
-- AUTO_INCREMENT pour la table `club`
--
ALTER TABLE `club`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT pour la table `cour`
--
ALTER TABLE `cour`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `post`
--
ALTER TABLE `post`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT pour la table `seance`
--
ALTER TABLE `seance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=86;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `blog`
--
ALTER TABLE `blog`
  ADD CONSTRAINT `FK_C0155143A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
