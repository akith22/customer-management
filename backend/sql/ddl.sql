-- =============================================================
--  ClientSphere — DDL
--  Database: customer_management (MariaDB)
--  Run this first, then dml.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS customer_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE customer_management;

-- -------------------------------------------------------------
--  COUNTRY  (master data — managed by admin, not via frontend)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS country (
                                       id   BIGINT       NOT NULL AUTO_INCREMENT,
                                       name VARCHAR(100) NOT NULL,
    code VARCHAR(10)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_country_code UNIQUE (code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
--  CITY  (master data — linked to country)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS city (
                                    id         BIGINT       NOT NULL AUTO_INCREMENT,
                                    name       VARCHAR(100) NOT NULL,
    country_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_city_country FOREIGN KEY (country_id)
    REFERENCES country (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
--  CUSTOMER
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
                                        id         BIGINT       NOT NULL AUTO_INCREMENT,
                                        name       VARCHAR(150) NOT NULL,
    dob        DATE         NOT NULL,
    nic        VARCHAR(20)  NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_customer_nic UNIQUE (nic)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
--  CUSTOMER_MOBILE  (one customer → many mobiles)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_mobile (
                                               id          BIGINT      NOT NULL AUTO_INCREMENT,
                                               customer_id BIGINT      NOT NULL,
                                               mobile      VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id)
    REFERENCES customer (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
--  CUSTOMER_ADDRESS  (one customer → many addresses)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_address (
                                                id            BIGINT       NOT NULL AUTO_INCREMENT,
                                                customer_id   BIGINT       NOT NULL,
                                                address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city_id       BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id)
    REFERENCES customer (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT fk_address_city FOREIGN KEY (city_id)
    REFERENCES city (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------------
--  CUSTOMER_FAMILY  (composite PK — bidirectional link table)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_family (
                                               customer_id      BIGINT NOT NULL,
                                               family_member_id BIGINT NOT NULL,
                                               PRIMARY KEY (customer_id, family_member_id),
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)
    REFERENCES customer (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT fk_family_member FOREIGN KEY (family_member_id)
    REFERENCES customer (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;