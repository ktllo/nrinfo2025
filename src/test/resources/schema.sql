-- This is a tune downed version of the database to provide only critical table that used by many function
-- e.g. configuration

CREATE TABLE IF NOT EXISTS configuration_group (
   group_name varchar(100) NOT NULL,
   group_description text DEFAULT NULL,
   PRIMARY KEY (group_name)
);

CREATE TABLE IF NOT EXISTS configuration (
     configuration_name varchar(100) NOT NULL,
     configuration_group varchar(100) DEFAULT NULL,
     configuration_value varchar(1000) DEFAULT NULL,
     data_type varchar(100) DEFAULT NULL,
     max_cache_time int(5) unsigned NOT NULL DEFAULT 3600,
     updated_by int(10) unsigned DEFAULT NULL,
     updated_date datetime DEFAULT NULL,
     PRIMARY KEY (configuration_name)
);