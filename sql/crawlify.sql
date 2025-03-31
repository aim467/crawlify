/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80100
 Source Host           : localhost:3306
 Source Schema         : crawlify

 Target Server Type    : MySQL
 Target Server Version : 80100
 File Encoding         : 65001

 Date: 31/03/2025 17:50:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for spider_task
-- ----------------------------
DROP TABLE IF EXISTS `spider_task`;
CREATE TABLE `spider_task`  (
  `task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `website_id` int NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for website_info
-- ----------------------------
DROP TABLE IF EXISTS `website_info`;
CREATE TABLE `website_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for website_link
-- ----------------------------
DROP TABLE IF EXISTS `website_link`;
CREATE TABLE `website_link`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `website_id` int NULL DEFAULT NULL,
  `url_type` tinyint NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT NULL,
  `ext_link` tinyint NULL DEFAULT NULL COMMENT '外部链接(1=true, 0=false)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `website_link_pk`(`website_id` ASC, `url` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17042 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
