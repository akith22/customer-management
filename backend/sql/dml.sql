-- =============================================================
--  ClientSphere — DML
--  Database: customer_management (MariaDB)
--  Run ddl.sql first, then this file
-- =============================================================

USE customer_management;

-- -------------------------------------------------------------
--  COUNTRY
-- -------------------------------------------------------------
INSERT IGNORE INTO country (id, name, code) VALUES
(1,  'Sri Lanka',      'LKA'),
(2,  'India',          'IND'),
(3,  'United Kingdom', 'GBR'),
(4,  'Australia',      'AUS'),
(5,  'United States',  'USA');

-- -------------------------------------------------------------
--  CITY
-- -------------------------------------------------------------
INSERT IGNORE INTO city (id, name, country_id) VALUES
-- Major cities
(1,  'Colombo',         1),
(2,  'Kandy',           1),
(3,  'Galle',           1),
(4,  'Negombo',         1),
(5,  'Kurunegala',      1),
(6,  'Jaffna',          1),
(7,  'Matara',          1),
(8,  'Ratnapura',       1),
(9,  'Badulla',         1),
(10, 'Trincomalee',     1),
(11, 'Batticaloa',      1),
(12, 'Anuradhapura',    1),
(13, 'Polonnaruwa',     1),
(14, 'Nuwara Eliya',    1),
(15, 'Ampara',          1),
(16, 'Hambantota',      1),
(17, 'Vavuniya',        1),
(18, 'Mannar',          1),
(19, 'Puttalam',        1),
(20, 'Kalmunai',        1),
-- Suburbs / Towns
(21, 'Malabe',          1),
(22, 'Maharagama',      1),
(23, 'Nugegoda',        1),
(24, 'Dehiwala',        1),
(25, 'Moratuwa',        1),
(26, 'Katubedda',       1),
(27, 'Piliyandala',     1),
(28, 'Homagama',        1),
(29, 'Kaduwela',        1),
(30, 'Kelaniya',        1),
(31, 'Wattala',         1),
(32, 'Ja-Ela',          1),
(33, 'Panadura',        1),
(34, 'Kalutara',        1),
(35, 'Beruwala',        1),
(36, 'Aluthgama',       1),
(37, 'Wadduwa',         1),
(38, 'Bandaragama',     1),
(39, 'Horana',          1),
(40, 'Ingiriya',        1),
(41, 'Avissawella',     1),
(42, 'Hanwella',        1),
(43, 'Kadawatha',       1),
(44, 'Ragama',          1),
(45, 'Gampaha',         1),
(46, 'Veyangoda',       1),
(47, 'Minuwangoda',     1),
(48, 'Katana',          1),
(49, 'Divulapitiya',    1),
(50, 'Mirigama',        1),
-- Upcountry / Rural
(51, 'MadolSima',       1),
(52, 'Bakamuna',        1),
(53, 'Embilipitiya',    1),
(54, 'Tissamaharama',   1),
(55, 'Tangalle',        1),
(56, 'Weligama',        1),
(57, 'Hikkaduwa',       1),
(58, 'Ambalangoda',     1),
(59, 'Balapitiya',      1),
(60, 'Elpitiya',        1),
(61, 'Akuressa',        1),
(62, 'Deniyaya',        1),
(63, 'Hakmana',         1),
(64, 'Mullaitivu',      1),
(65, 'Kilinochchi',     1),
(66, 'Puttalam',        1),
(67, 'Chilaw',          1),
(68, 'Marawila',        1),
(69, 'Kuliyapitiya',    1),
(70, 'Wariyapola',      1),
-- India
(71, 'Mumbai',          2),
(72, 'Chennai',         2),
(73, 'Bangalore',       2),
-- UK
(74, 'London',          3),
(75, 'Manchester',      3),
-- Australia
(76, 'Sydney',          4),
(77, 'Melbourne',       4),
-- US
(78, 'New York',        5),
(79, 'Los Angeles',     5);

-- -------------------------------------------------------------
--  CUSTOMER
-- -------------------------------------------------------------
INSERT IGNORE INTO customer (id, name, dob, nic) VALUES
(1,  'Chamara Bandara',        '1990-03-15', '199032580123'),
(2,  'Dilani Rathnayake',      '1985-07-22', '198574310456'),
(3,  'Ruwan Abeysekara',       '1993-11-04', '199364820789'),
(4,  'Nethmi Jayawardena',     '1998-01-30', '199813450012'),
(5,  'Kasun Wickramasinghe',   '1987-09-18', '198794650345'),
(6,  'Tharaka Dissanayake',    '1992-05-08', '199254830671'),
(7,  'Prasadi Fernando',       '1996-12-19', '199664920183'),
(8,  'Maduranga Silva',        '1983-04-27', '198344710294'),
(9,  'Oshadi Perera',          '2000-08-03', '200084520405'),
(10, 'Lasith Gunawardena',     '1989-02-14', '198924630516'),
(11, 'Binara Senanayake',      '1994-06-12', '199461230789'),
(12, 'Sachini Wijeratne',      '1991-09-25', '199192450123'),
(13, 'Dilan Priyashantha',     '1997-03-07', '199730720456'),
(14, 'Amaya Kodithuwakku',     '1988-11-18', '198811840789'),
(15, 'Ranidu Samaraweera',     '2001-04-30', '200144300012');

-- -------------------------------------------------------------
--  CUSTOMER_MOBILE
-- -------------------------------------------------------------
INSERT IGNORE INTO customer_mobile (customer_id, mobile) VALUES
(1,  '0771234567'),
(1,  '0112345678'),
(2,  '0762345678'),
(3,  '0753456789'),
(4,  '0774567890'),
(5,  '0785678901'),
(5,  '0115678901'),
(6,  '0701122334'),
(7,  '0712233445'),
(8,  '0723344556'),
(9,  '0734455667'),
(10, '0745566778'),
(11, '0711223344'),
(11, '0117223344'),
(12, '0722334455'),
(13, '0733445566'),
(14, '0744556677'),
(15, '0755667788');

-- -------------------------------------------------------------
--  CUSTOMER_ADDRESS
-- city_id: 51=MadolSima, 52=Bakamuna, 26=Katubedda, 1=Colombo
-- -------------------------------------------------------------
INSERT IGNORE INTO customer_address (customer_id, address_line1, address_line2, city_id) VALUES
(1,  'No 22, Lake Road',        'Apt 5',  51),
(2,  'No 78, Temple Street',    'Apt 12', 52),
(3,  'No 5, Galle Road',        'Apt 3',  26),
(4,  'No 130, Station Road',    'Apt 8',  51),
(5,  'No 47, Raja Mawatha',     'Apt 21', 52),
(6,  'No 9, Kandy Road',        'Apt 2',  26),
(7,  'No 56, Beach Avenue',     'Apt 17', 51),
(8,  'No 200, Hill Street',     'Apt 6',  52),
(9,  'No 33, Church Road',      'Apt 9',  26),
(10, 'No 88, Colombo Street',   'Apt 14', 51),
(11, 'No 17, Flower Road',      'Apt 3',  51),
(11, 'No 2, Baseline Road',     NULL,     1),
(12, 'No 63, River Lane',       'Apt 11', 52),
(13, 'No 8, Lotus Avenue',      'Apt 7',  26),
(14, 'No 102, Sunset Drive',    'Apt 19', 51),
(15, 'No 29, Marine Terrace',   'Apt 5',  52);

-- -------------------------------------------------------------
--  CUSTOMER_FAMILY
-- -------------------------------------------------------------
INSERT IGNORE INTO customer_family (customer_id, family_member_id) VALUES
(1,  2),
(2,  1),
(3,  4),
(4,  3),
(6,  7),
(7,  6),
(11, 12),
(12, 11),
(13, 14),
(14, 13);