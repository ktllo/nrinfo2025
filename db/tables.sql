/*M!999999\- enable the sandbox mode */
-- MariaDB dump 10.19  Distrib 10.11.11-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: nrinfo
-- ------------------------------------------------------
-- Server version       10.11.11-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `SPRING_SESSION`
--

DROP TABLE IF EXISTS `SPRING_SESSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SPRING_SESSION` (
                                  `PRIMARY_ID` char(36) NOT NULL,
                                  `SESSION_ID` char(36) NOT NULL,
                                  `CREATION_TIME` bigint(20) NOT NULL,
                                  `LAST_ACCESS_TIME` bigint(20) NOT NULL,
                                  `MAX_INACTIVE_INTERVAL` int(11) NOT NULL,
                                  `EXPIRY_TIME` bigint(20) NOT NULL,
                                  `PRINCIPAL_NAME` varchar(100) DEFAULT NULL,
                                  PRIMARY KEY (`PRIMARY_ID`),
                                  UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
                                  KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
                                  KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SPRING_SESSION_ATTRIBUTES`
--

DROP TABLE IF EXISTS `SPRING_SESSION_ATTRIBUTES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `SPRING_SESSION_ATTRIBUTES` (
                                             `SESSION_PRIMARY_ID` char(36) NOT NULL,
                                             `ATTRIBUTE_NAME` varchar(200) NOT NULL,
                                             `ATTRIBUTE_BYTES` blob NOT NULL,
                                             PRIMARY KEY (`SESSION_PRIMARY_ID`,`ATTRIBUTE_NAME`),
                                             CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `SPRING_SESSION` (`PRIMARY_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration` (
                                 `configuration_name` varchar(100) NOT NULL,
                                 `configuration_group` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                                 `configuration_value` varchar(1000) DEFAULT NULL,
                                 `data_type` varchar(100) DEFAULT NULL,
                                 `max_cache_time` smallint(5) unsigned NOT NULL DEFAULT 3600,
                                 `updated_by` int(10) unsigned DEFAULT NULL,
                                 `updated_date` datetime DEFAULT NULL,
                                 PRIMARY KEY (`configuration_name`),
                                 KEY `configuration_configuration_group_IDX` (`configuration_group`) USING BTREE,
                                 CONSTRAINT `configuration_configuration_group_FK` FOREIGN KEY (`configuration_group`) REFERENCES `configuration_group` (`group_name`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_group`
--

DROP TABLE IF EXISTS `configuration_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration_group` (
                                       `group_name` varchar(100) NOT NULL,
                                       `group_description` text DEFAULT NULL,
                                       PRIMARY KEY (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `corpus`
--

DROP TABLE IF EXISTS `corpus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `corpus` (
                          `nlc_code` char(7) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
                          `stanox` char(5) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                          `uic_code` varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                          `crs_code` char(3) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                          `tiploc_code` char(7) DEFAULT NULL,
                          `nlc_desc` varchar(100) DEFAULT NULL,
                          `nlc_desc_16` varchar(100) DEFAULT NULL,
                          PRIMARY KEY (`nlc_code`),
                          KEY `corpus_nlc_desc_IDX` (`nlc_desc`) USING BTREE,
                          KEY `corpus_stanox_IDX` (`stanox`) USING BTREE,
                          KEY `corpus_uic_code_IDX` (`uic_code`) USING BTREE,
                          KEY `corpus_crs_code_IDX` (`crs_code`) USING BTREE,
                          KEY `corpus_tiploc_code_IDX` (`tiploc_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Codes for Operations, Retail & Planning – a Unified Solution';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `job` (
                       `job_id` binary(16) NOT NULL,
                       `job_owner` int(10) unsigned NOT NULL,
                       `job_class` varchar(255) NOT NULL,
                       `submitted_time` datetime DEFAULT NULL,
                       `started_time` datetime DEFAULT NULL,
                       `finished_time` datetime DEFAULT NULL,
                       `job_status` char(1) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                       PRIMARY KEY (`job_id`),
                       KEY `job_job_owner_IDX` (`job_owner`) USING BTREE,
                       CONSTRAINT `job_user_FK` FOREIGN KEY (`job_owner`) REFERENCES `user` (`user_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_output`
--

DROP TABLE IF EXISTS `job_output`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_output` (
                              `job_output_id` binary(16) NOT NULL,
                              `job_id` binary(16) NOT NULL,
                              `output_type` char(1) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                              `output_data` text DEFAULT NULL,
                              `message_time` datetime DEFAULT NULL,
                              PRIMARY KEY (`job_output_id`),
                              KEY `job_output_job_id_IDX` (`job_id`) USING BTREE,
                              CONSTRAINT `job_output_job_FK` FOREIGN KEY (`job_id`) REFERENCES `job` (`job_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `naptan_stoppoint`
--

DROP TABLE IF EXISTS `naptan_stoppoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `naptan_stoppoint` (
                                    `atoc_code` char(12) NOT NULL,
                                    `naptan_code` varchar(12) DEFAULT NULL,
                                    `plate_code` varchar(12) DEFAULT NULL,
                                    `cleardown_code` bigint(20) unsigned DEFAULT NULL,
                                    `common_name` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `short_common_name` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `landmark` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `street` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `crossing` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `indicator` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `bearing` char(2) DEFAULT NULL,
                                    `nptg_code` char(8) DEFAULT NULL,
                                    `town` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `suburb` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `locality_centre` char(1) DEFAULT NULL,
                                    `grid_type` varchar(10) DEFAULT NULL,
                                    `easting` int(11) DEFAULT NULL,
                                    `northing` int(11) DEFAULT NULL,
                                    `longitude` decimal(10,7) DEFAULT NULL,
                                    `latitude` decimal(10,7) DEFAULT NULL,
                                    `stop_type` char(3) DEFAULT NULL,
                                    `bus_stop_type` char(3) DEFAULT NULL,
                                    `updated_date` datetime DEFAULT NULL,
                                    `revision_number` int(10) unsigned DEFAULT NULL,
                                    PRIMARY KEY (`atoc_code`),
                                    KEY `naptan_stoppoint_naptan_code_IDX` (`naptan_code`) USING BTREE,
                                    KEY `naptan_stoppoint_nptg_code_IDX` (`nptg_code`) USING BTREE,
                                    KEY `naptan_stoppoint_easting_IDX` (`easting`,`northing`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nptg_localities`
--

DROP TABLE IF EXISTS `nptg_localities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `nptg_localities` (
                                   `nptg_code` char(8) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
                                   `locality_name` varchar(48) DEFAULT NULL,
                                   `short_name` varchar(48) DEFAULT NULL,
                                   `qualifier_name` varchar(48) DEFAULT NULL,
                                   `admin_area_code` char(8) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                                   `district_code` char(8) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                                   `grid_type` varchar(10) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                                   `easting` int(11) DEFAULT NULL,
                                   `northing` int(11) DEFAULT NULL,
                                   `longitude` decimal(10,7) DEFAULT NULL,
                                   `latitude` decimal(10,7) DEFAULT NULL,
                                   `updated_date` datetime DEFAULT NULL,
                                   `revision_number` int(10) unsigned DEFAULT NULL,
                                   PRIMARY KEY (`nptg_code`),
                                   KEY `nptg_localities_admin_area_code_IDX` (`admin_area_code`) USING BTREE,
                                   KEY `nptg_localities_district_code_IDX` (`district_code`) USING BTREE,
                                   KEY `nptg_localities_easting_IDX` (`easting`,`northing`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `password_reset_request`
--

DROP TABLE IF EXISTS `password_reset_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_request` (
                                          `request_id` binary(16) NOT NULL,
                                          `request_key` char(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
                                          `user_id` int(10) unsigned NOT NULL,
                                          `requested_date` datetime DEFAULT NULL,
                                          `expiry_date` datetime DEFAULT NULL,
                                          `processed_date` datetime DEFAULT NULL,
                                          `status` char(1) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL,
                                          PRIMARY KEY (`request_id`),
                                          UNIQUE KEY `password_reset_request_unique` (`request_key`),
                                          KEY `password_reset_request_user_id_IDX` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
                              `permission_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                              `permission_name` varchar(100) DEFAULT NULL,
                              `description` text DEFAULT NULL,
                              PRIMARY KEY (`permission_id`),
                              KEY `permission_permission_name_IDX` (`permission_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
                        `role_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                        `role_name` varchar(100) NOT NULL,
                        `role_description` text DEFAULT NULL,
                        PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
                                   `role_id` int(10) unsigned NOT NULL,
                                   `permission_id` int(10) unsigned NOT NULL,
                                   PRIMARY KEY (`role_id`,`permission_id`),
                                   KEY `role_permission_permission_FK` (`permission_id`),
                                   CONSTRAINT `role_permission_permission_FK` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                   CONSTRAINT `role_permission_role_FK` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `schedule_association`
--

DROP TABLE IF EXISTS `schedule_association`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_association` (
                                        `base_uid` char(6) NOT NULL,
                                        `assoc_uid` char(6) NOT NULL,
                                        `start_date` date NOT NULL,
                                        `end_date` date NOT NULL,
                                        `assoc_days` char(7) NOT NULL,
                                        `stp_indicator` char(1) NOT NULL,
                                        `assoc_date` char(1) DEFAULT NULL,
                                        `assoc_location` char(7) DEFAULT NULL,
                                        `base_suffix` char(1) DEFAULT NULL,
                                        `assoc_suffix` varchar(100) DEFAULT NULL,
                                        `assoc_category` char(1) DEFAULT NULL,
                                        `assoc_type` char(1) DEFAULT NULL,
                                        `created_date` datetime NOT NULL,
                                        PRIMARY KEY (`base_uid`,`assoc_uid`,`start_date`,`end_date`,`assoc_days`,`stp_indicator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tiploc`
--

DROP TABLE IF EXISTS `tiploc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tiploc` (
                          `tiploc_code` char(7) NOT NULL,
                          `nalco` char(6) DEFAULT NULL,
                          `stanox` char(5) DEFAULT NULL,
                          `crs_code` char(3) DEFAULT NULL,
                          `description` varchar(100) DEFAULT NULL,
                          `tps_description` varchar(100) DEFAULT NULL,
                          PRIMARY KEY (`tiploc_code`),
                          KEY `tiploc_nalco_IDX` (`nalco`) USING BTREE,
                          KEY `tiploc_stanox_IDX` (`stanox`) USING BTREE,
                          KEY `tiploc_crs_code_IDX` (`crs_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `user_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                        `username` varchar(100) NOT NULL,
                        `password` varchar(100) DEFAULT NULL,
                        `last_password_date` datetime DEFAULT NULL,
                        `created_date` datetime DEFAULT NULL,
                        `updated_date` datetime DEFAULT NULL,
                        `force_password_change` tinyint(1) NOT NULL DEFAULT 0,
                        `last_login` datetime DEFAULT NULL,
                        `failed_login_count` mediumint(8) unsigned DEFAULT 0,
                        `last_failed_login` datetime DEFAULT NULL,
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY `users_username_IDX` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=ascii COLLATE=ascii_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_permission`
--

DROP TABLE IF EXISTS `user_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permission` (
                                   `user_id` int(10) unsigned NOT NULL,
                                   `permission_id` int(10) unsigned NOT NULL,
                                   PRIMARY KEY (`user_id`,`permission_id`),
                                   KEY `user_permission_permission_FK` (`permission_id`),
                                   CONSTRAINT `user_permission_permission_FK` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                   CONSTRAINT `user_permission_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_property`
--

DROP TABLE IF EXISTS `user_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_property` (
                                 `property_name` varchar(100) NOT NULL,
                                 `description` text DEFAULT NULL,
                                 PRIMARY KEY (`property_name`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_property_detail`
--

DROP TABLE IF EXISTS `user_property_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_property_detail` (
                                        `user_id` int(10) unsigned NOT NULL,
                                        `property_name` varchar(100) NOT NULL,
                                        `property_value` text DEFAULT NULL,
                                        PRIMARY KEY (`user_id`,`property_name`),
                                        KEY `user_permission_detail_user_parameter_FK` (`property_name`),
                                        CONSTRAINT `user_permission_detail_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                        CONSTRAINT `user_permission_detail_user_parameter_FK` FOREIGN KEY (`property_name`) REFERENCES `user_property` (`property_name`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii COLLATE=ascii_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
                             `user_id` int(10) unsigned NOT NULL,
                             `role_id` int(10) unsigned NOT NULL,
                             PRIMARY KEY (`user_id`,`role_id`),
                             KEY `user_role_role_FK` (`role_id`),
                             CONSTRAINT `user_role_role_FK` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                             CONSTRAINT `user_role_user_FK` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'nrinfo'
--
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP FUNCTION IF EXISTS `get_date_mask` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`nrinfo`@`%` FUNCTION `get_date_mask`(val date) RETURNS char(7) CHARSET utf8mb4 COLLATE utf8mb4_bin
    DETERMINISTIC
begin
    return case weekday(val)
               when 0 then '1______'
               when 1 then '_1_____'
               when 2 then '__1____'
               when 3 then '___1___'
               when 4 then '____1__'
               when 5 then '_____1_'
               when 6 then '______1'
               else null
        end;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-07 11:01:47