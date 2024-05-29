CREATE TABLE IF NOT EXISTS `mid_user` (
  `id` binary(16) PRIMARY KEY NOT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `last_update` datetime(6) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mid_role` (
  `id` int PRIMARY KEY NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mid_user_role` (
  `user_id` binary(16) NOT NULL,
  `role_id` int NOT NULL,
  CONSTRAINT `mid_user_role_pk` PRIMARY KEY (`role_id`,`user_id`),
  CONSTRAINT `mid_user_role_fk_role` FOREIGN KEY (`role_id`) REFERENCES `mid_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `mid_user_role_fk_user` FOREIGN KEY (`user_id`) REFERENCES `mid_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mid_login_history` (
  `id` binary(16) PRIMARY KEY NOT NULL,
  `login_at` datetime(6) DEFAULT NULL,
  `success` bit(1) DEFAULT NULL,
  `user_id` binary(16) NOT NULL,
  CONSTRAINT `mid_login_history_fk_user` FOREIGN KEY (`user_id`) REFERENCES `mid_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `mid_role` (`id`, `active`, `name`) VALUES (1, 1, 'USER');
INSERT INTO `mid_role` (`id`, `active`, `name`) VALUES (2, 1, 'ADMIN');

INSERT INTO `mid_user` (`id`, `is_active`, `created_on`, `email`, `last_update`, `password`)
VALUES (UUID_TO_BIN(UUID(), true), 1, CURRENT_TIMESTAMP(), 'admin@microid.com', CURRENT_TIMESTAMP(), '$2a$10$VtLnU/ZYARTQOThiocEuue3w6xLMi0Z/PO3KvSBwwDEWuaHN2tfq2');

INSERT INTO `mid_user_role` (`user_id`, `role_id`) VALUES ((SELECT `id` FROM `mid_user` WHERE `email` = 'admin@microid.com'), 1);
INSERT INTO `mid_user_role` (`user_id`, `role_id`) VALUES ((SELECT `id` FROM `mid_user` WHERE `email` = 'admin@microid.com'), 2);
