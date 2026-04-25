-- =============================================================
--  ClientSphere — DML  (Master Data Seed)
--  Run AFTER ddl.sql
-- =============================================================

USE customer_management;

-- =============================================================
--  COUNTRIES
-- =============================================================
INSERT INTO country (name, code) VALUES
                                     ('Sri Lanka',       'LK'),
                                     ('India',           'IN'),
                                     ('United Kingdom',  'GB'),
                                     ('United States',   'US'),
                                     ('Australia',       'AU'),
                                     ('Canada',          'CA'),
                                     ('Germany',         'DE'),
                                     ('France',          'FR'),
                                     ('Singapore',       'SG'),
                                     ('United Arab Emirates', 'AE'),
                                     ('Japan',           'JP'),
                                     ('China',           'CN'),
                                     ('Pakistan',        'PK'),
                                     ('Bangladesh',      'BD'),
                                     ('Malaysia',        'MY'),
                                     ('Maldives',        'MV'),
                                     ('New Zealand',     'NZ'),
                                     ('South Africa',    'ZA'),
                                     ('Italy',           'IT'),
                                     ('Netherlands',     'NL');

-- =============================================================
--  CITIES — SRI LANKA  (comprehensive)
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Colombo'         AS name UNION ALL
         SELECT 'Kandy'                   UNION ALL
         SELECT 'Galle'                   UNION ALL
         SELECT 'Jaffna'                  UNION ALL
         SELECT 'Matara'                  UNION ALL
         SELECT 'Negombo'                 UNION ALL
         SELECT 'Anuradhapura'            UNION ALL
         SELECT 'Trincomalee'             UNION ALL
         SELECT 'Batticaloa'              UNION ALL
         SELECT 'Badulla'                 UNION ALL
         SELECT 'Ratnapura'               UNION ALL
         SELECT 'Kurunegala'              UNION ALL
         SELECT 'Puttalam'                UNION ALL
         SELECT 'Polonnaruwa'             UNION ALL
         SELECT 'Hambantota'              UNION ALL
         SELECT 'Vavuniya'                UNION ALL
         SELECT 'Mannar'                  UNION ALL
         SELECT 'Ampara'                  UNION ALL
         SELECT 'Monaragala'              UNION ALL
         SELECT 'Nuwara Eliya'            UNION ALL
         SELECT 'Kegalle'                 UNION ALL
         SELECT 'Kalutara'                UNION ALL
         SELECT 'Gampaha'                 UNION ALL
         SELECT 'Matale'                  UNION ALL
         SELECT 'Mullaitivu'              UNION ALL
         SELECT 'Kilinochchi'             UNION ALL
         SELECT 'Tangalle'                UNION ALL
         SELECT 'Weligama'                UNION ALL
         SELECT 'Hikkaduwa'               UNION ALL
         SELECT 'Bentota'                 UNION ALL
         SELECT 'Beruwala'                UNION ALL
         SELECT 'Panadura'                UNION ALL
         SELECT 'Moratuwa'                UNION ALL
         SELECT 'Dehiwala'                UNION ALL
         SELECT 'Mount Lavinia'           UNION ALL
         SELECT 'Kaduwela'                UNION ALL
         SELECT 'Maharagama'              UNION ALL
         SELECT 'Nugegoda'                UNION ALL
         SELECT 'Rajagiriya'              UNION ALL
         SELECT 'Kottawa'                 UNION ALL
         SELECT 'Piliyandala'             UNION ALL
         SELECT 'Homagama'                UNION ALL
         SELECT 'Avissawella'             UNION ALL
         SELECT 'Kadawatha'               UNION ALL
         SELECT 'Kelaniya'                UNION ALL
         SELECT 'Ja-Ela'                  UNION ALL
         SELECT 'Wattala'                 UNION ALL
         SELECT 'Minuwangoda'             UNION ALL
         SELECT 'Chilaw'                  UNION ALL
         SELECT 'Kuliyapitiya'
     ) c CROSS JOIN country co WHERE co.code = 'LK';

-- =============================================================
--  CITIES — INDIA
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Mumbai'    AS name UNION ALL
         SELECT 'Delhi'             UNION ALL
         SELECT 'Bangalore'         UNION ALL
         SELECT 'Chennai'           UNION ALL
         SELECT 'Kolkata'           UNION ALL
         SELECT 'Hyderabad'         UNION ALL
         SELECT 'Pune'              UNION ALL
         SELECT 'Ahmedabad'         UNION ALL
         SELECT 'Jaipur'            UNION ALL
         SELECT 'Surat'
     ) c CROSS JOIN country co WHERE co.code = 'IN';

-- =============================================================
--  CITIES — UNITED KINGDOM
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'London'        AS name UNION ALL
         SELECT 'Manchester'            UNION ALL
         SELECT 'Birmingham'            UNION ALL
         SELECT 'Leeds'                 UNION ALL
         SELECT 'Glasgow'               UNION ALL
         SELECT 'Liverpool'             UNION ALL
         SELECT 'Edinburgh'             UNION ALL
         SELECT 'Bristol'               UNION ALL
         SELECT 'Sheffield'             UNION ALL
         SELECT 'Leicester'
     ) c CROSS JOIN country co WHERE co.code = 'GB';

-- =============================================================
--  CITIES — UNITED STATES
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'New York'      AS name UNION ALL
         SELECT 'Los Angeles'           UNION ALL
         SELECT 'Chicago'               UNION ALL
         SELECT 'Houston'               UNION ALL
         SELECT 'Phoenix'               UNION ALL
         SELECT 'Philadelphia'          UNION ALL
         SELECT 'San Antonio'           UNION ALL
         SELECT 'San Diego'             UNION ALL
         SELECT 'Dallas'                UNION ALL
         SELECT 'San Francisco'
     ) c CROSS JOIN country co WHERE co.code = 'US';

-- =============================================================
--  CITIES — AUSTRALIA
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Sydney'       AS name UNION ALL
         SELECT 'Melbourne'            UNION ALL
         SELECT 'Brisbane'             UNION ALL
         SELECT 'Perth'                UNION ALL
         SELECT 'Adelaide'             UNION ALL
         SELECT 'Canberra'             UNION ALL
         SELECT 'Gold Coast'           UNION ALL
         SELECT 'Newcastle'
     ) c CROSS JOIN country co WHERE co.code = 'AU';

-- =============================================================
--  CITIES — CANADA
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Toronto'      AS name UNION ALL
         SELECT 'Vancouver'            UNION ALL
         SELECT 'Montreal'             UNION ALL
         SELECT 'Calgary'              UNION ALL
         SELECT 'Ottawa'               UNION ALL
         SELECT 'Edmonton'
     ) c CROSS JOIN country co WHERE co.code = 'CA';

-- =============================================================
--  CITIES — UAE, SINGAPORE, MALAYSIA, MALDIVES
-- =============================================================
INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Dubai'        AS name UNION ALL
         SELECT 'Abu Dhabi'            UNION ALL
         SELECT 'Sharjah'
     ) c CROSS JOIN country co WHERE co.code = 'AE';

INSERT INTO city (name, country_id) SELECT 'Singapore', co.id
FROM country co WHERE co.code = 'SG';

INSERT INTO city (name, country_id) SELECT c.name, co.id
FROM (
         SELECT 'Kuala Lumpur' AS name UNION ALL
         SELECT 'Penang'               UNION ALL
         SELECT 'Johor Bahru'
     ) c CROSS JOIN country co WHERE co.code = 'MY';

INSERT INTO city (name, country_id) SELECT 'Male', co.id
FROM country co WHERE co.code = 'MV';

-- =============================================================
--  VERIFY (optional — comment out in production)
-- =============================================================
-- SELECT co.name AS country, COUNT(ci.id) AS city_count
-- FROM country co
-- LEFT JOIN city ci ON ci.country_id = co.id
-- GROUP BY co.name
-- ORDER BY city_count DESC;