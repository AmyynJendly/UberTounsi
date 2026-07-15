-- ============================================================
-- CovoitDark — STANDALONE RESET & SEED
-- Password for ALL users: Covoit2026!
-- Run: mysql -u root -p < seed_test.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS covoiturage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE covoiturage;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. DROP TABLES
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS disputes;
DROP TABLE IF EXISTS trip_chat_messages;
DROP TABLE IF EXISTS driver_favorites;
DROP TABLE IF EXISTS promo_codes;
DROP TABLE IF EXISTS otp_verifications;
DROP TABLE IF EXISTS stats;
DROP TABLE IF EXISTS saved_searches;
DROP TABLE IF EXISTS badges;
DROP TABLE IF EXISTS blocks;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS payment_methods;

-- 2. CREATE TABLES (based on schema.sql)

CREATE TABLE users (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    password        VARCHAR(256) NOT NULL,
    salt            VARCHAR(64)  NOT NULL,
    phone           VARCHAR(20),
    role            ENUM('PASSENGER','DRIVER','ADMIN') NOT NULL DEFAULT 'PASSENGER',
    bio             TEXT,
    languages       VARCHAR(500),
    reputation_score DOUBLE DEFAULT 0.0,
    is_verified     BOOLEAN DEFAULT FALSE,
    is_blocked      BOOLEAN DEFAULT FALSE,
    failed_logins   INT DEFAULT 0,
    balance         DOUBLE DEFAULT 0.0,
    avatar          TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_verified     BOOLEAN DEFAULT FALSE,
    cancellation_rate DOUBLE DEFAULT 0.0,
    id_document     TEXT DEFAULT NULL
);

CREATE TABLE cars (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    driver_id       INT NOT NULL,
    brand           VARCHAR(50) NOT NULL,
    model           VARCHAR(50) NOT NULL,
    color           VARCHAR(30) NOT NULL,
    seats           INT NOT NULL DEFAULT 4,
    license_plate   VARCHAR(20) UNIQUE NOT NULL,
    image_base64    LONGTEXT,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trips (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    driver_id       INT NOT NULL,
    car_id          INT NOT NULL,
    departure       VARCHAR(100) NOT NULL,
    arrival         VARCHAR(100) NOT NULL,
    departure_time  TIME NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    repeat_days     VARCHAR(100),
    available_seats INT NOT NULL,
    price           DOUBLE NOT NULL,
    luggage_policy  ENUM('NONE','SMALL','LARGE') DEFAULT 'SMALL',
    music_preference ENUM('NONE','SOFT','ANY') DEFAULT 'ANY',
    smoking_allowed BOOLEAN DEFAULT FALSE,
    ac_available    BOOLEAN DEFAULT TRUE,
    flexible_pickup BOOLEAN DEFAULT FALSE,
    women_only      BOOLEAN DEFAULT FALSE,
    pets_allowed    BOOLEAN DEFAULT FALSE,
    status          ENUM('ACTIVE','COMPLETE','CANCELLED') DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id)
);

CREATE TABLE requests (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    trip_id         INT NOT NULL,
    passenger_id    INT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    total_cost      DOUBLE NOT NULL DEFAULT 0.0,
    secret_code     VARCHAR(10),
    status          ENUM('PENDING','ACCEPTED','REJECTED','WAITLIST','CANCELLED','COMPLETE','NEGOTIATING') DEFAULT 'PENDING',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    luggage_size    ENUM('NONE','SMALL','LARGE') DEFAULT 'SMALL',
    counter_price   DOUBLE DEFAULT NULL,
    promo_code      VARCHAR(30) DEFAULT NULL,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ratings (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    trip_id         INT NOT NULL,
    rater_id        INT NOT NULL,
    rated_id        INT NOT NULL,
    score           INT NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_rating (trip_id, rater_id, rated_id),
    FOREIGN KEY (trip_id) REFERENCES trips(id),
    FOREIGN KEY (rater_id) REFERENCES users(id),
    FOREIGN KEY (rated_id) REFERENCES users(id)
);

CREATE TABLE messages (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    request_id      INT DEFAULT NULL,
    sender_id       INT NOT NULL,
    receiver_id     INT NOT NULL,
    content         TEXT NOT NULL,
    is_quick_response BOOLEAN DEFAULT FALSE,
    is_read         BOOLEAN DEFAULT FALSE,
    sent_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE reports (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    reporter_id     INT NOT NULL,
    reported_id     INT NOT NULL,
    reason          TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (reported_id) REFERENCES users(id)
);

CREATE TABLE blocks (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    blocker_id      INT NOT NULL,
    blocked_id      INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_block (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users(id),
    FOREIGN KEY (blocked_id) REFERENCES users(id)
);

CREATE TABLE badges (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    badge_name      VARCHAR(100) NOT NULL,
    awarded_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE saved_searches (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    passenger_id    INT NOT NULL,
    name            VARCHAR(100),
    departure       VARCHAR(100) NOT NULL,
    arrival         VARCHAR(100) NOT NULL,
    preferred_time  TIME,
    frequency       VARCHAR(50),
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE stats (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT UNIQUE NOT NULL,
    total_trips     INT DEFAULT 0,
    money_saved     DOUBLE DEFAULT 0.0,
    co2_saved       DOUBLE DEFAULT 0.0,
    money_earned    DOUBLE DEFAULT 0.0,
    cancelled_trips INT DEFAULT 0,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE otp_verifications (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  DATETIME NOT NULL,
    used        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE promo_codes (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(30) UNIQUE NOT NULL,
    discount_pct    DOUBLE NOT NULL DEFAULT 0.0,
    discount_fixed  DOUBLE NOT NULL DEFAULT 0.0,
    max_uses        INT DEFAULT 100,
    used_count      INT DEFAULT 0,
    expires_at      DATETIME,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE driver_favorites (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    driver_id    INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_fav (passenger_id, driver_id),
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)    REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trip_chat_messages (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    trip_id     INT NOT NULL,
    sender_id   INT NOT NULL,
    content     TEXT NOT NULL,
    sent_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id)   REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    actor_id    INT NOT NULL,
    action      VARCHAR(60) NOT NULL,
    target_type VARCHAR(40),
    target_id   INT,
    detail      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE disputes (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    trip_id         INT NOT NULL,
    complainant_id  INT NOT NULL,
    reason          TEXT NOT NULL,
    status          ENUM('OPEN','RESOLVED','DISMISSED') DEFAULT 'OPEN',
    admin_note      TEXT,
    resolved_at     DATETIME,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id)         REFERENCES trips(id),
    FOREIGN KEY (complainant_id)  REFERENCES users(id)
);

CREATE TABLE payment_methods (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    type            VARCHAR(50) NOT NULL, -- 'Card', 'D17', 'VISA', etc.
    last_four       VARCHAR(10),
    brand           VARCHAR(50), -- 'Visa', 'Mastercard', 'D17', etc.
    expiry          VARCHAR(10), -- 'MM/YY' or 'N/A'
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_trips_search ON trips(departure(50), arrival(50), start_date, status);
CREATE INDEX idx_trips_driver ON trips(driver_id, status);
CREATE INDEX idx_requests_passenger ON requests(passenger_id, status);
CREATE INDEX idx_requests_trip ON requests(trip_id, status);
CREATE INDEX idx_messages_receiver ON messages(receiver_id, sent_at);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_ratings_rated ON ratings(rated_id);
CREATE INDEX idx_otp_user      ON otp_verifications(user_id, used);
CREATE INDEX idx_trip_chat     ON trip_chat_messages(trip_id, sent_at);
CREATE INDEX idx_disputes      ON disputes(trip_id, status);
CREATE INDEX idx_fav_passenger ON driver_favorites(passenger_id);
CREATE INDEX idx_audit_actor   ON audit_log(actor_id, created_at);

-- 3. INSERT DATA (password = Covoit2026! for all)
-- Hash: 6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9
-- Salt: 6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=

INSERT INTO users (id, full_name, email, password, salt, phone, role, avatar, bio, languages, reputation_score, is_verified, id_verified, is_blocked, balance, cancellation_rate) VALUES
(1,  'Admin Système',    'admin@covoitdark.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 20 000 001', 'ADMIN', NULL, 'Administrateur principal.', 'Français, Arabe', 5.0, 1, 1, 0, 1000.0, 0.0),
(2,  'Youssef Ben Ali',  'youssef@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 22 345 678', 'DRIVER', NULL, 'Chauffeur expérimenté.', 'Arabe, Français', 4.8, 1, 1, 0, 320.0, 0.02),
(3,  'Oumayma Trabelsi', 'oumayma@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 25 987 321', 'DRIVER', NULL, 'Conduite douce.', 'Français, Anglais', 4.9, 1, 1, 0, 180.0, 0.01),
(4,  'Attia Hamdi',      'attia@pass.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 23 654 987', 'PASSENGER', NULL, 'Étudiant.', 'Arabe, Français', 4.5, 1, 0, 0, 200.0, 0.05),
(5,  'Karim Mansour',    'karim@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 94 112 233', 'DRIVER', NULL, 'Trajets réguliers.', 'Arabe, Français', 3.2, 1, 0, 0, 50.0, 0.15),
(6,  'Salma Kchaou',     'salma@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 55 778 899', 'DRIVER', NULL, 'Conductrice pro.', 'Français, Arabe', 4.95, 1, 1, 0, 410.0, 0.0),
(7,  'Wael Riahi',       'wael@pass.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 50 321 456', 'PASSENGER', NULL, 'Ingénieur.', 'Arabe, Anglais', 4.2, 1, 0, 0, 150.0, 0.04),
(8,  'Mouna Ayari',      'mouna@pass.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 98 456 123', 'PASSENGER', NULL, 'Professeure.', 'Anglais, Français', 5.0, 1, 1, 0, 450.0, 0.0),
(9,  'Fares Ghribi',     'fares@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 97 445 566', 'DRIVER', NULL, 'Ponctuel.', 'Arabe, Anglais', 4.6, 1, 1, 0, 230.0, 0.03),
(10, 'Nour Belhaj',      'nour@pass.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 52 887 664', 'PASSENGER', NULL, 'Architecte.', 'Français, Arabe', 4.7, 1, 0, 0, 300.0, 0.02),
(11, 'Slim Bourguiba',   'slim@pass.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 99 112 334', 'PASSENGER', NULL, 'Commercial.', 'Arabe', 3.9, 0, 0, 0, 80.0, 0.08);

INSERT INTO cars (id, driver_id, brand, model, color, seats, license_plate, image_base64) VALUES
(1, 2, 'Volkswagen', 'Golf 7', 'Noir', 4, 'RS 202 5555', NULL),
(2, 3, 'Peugeot', '3008', 'Gris', 4, 'TN 150 1234', NULL),
(3, 5, 'Renault', 'Clio 4', 'Rouge', 3, 'TN 195 9876', NULL),
(4, 6, 'Kia', 'Sportage', 'Blanc', 4, 'TN 210 4444', NULL),
(5, 9, 'Toyota', 'Corolla', 'Argent', 4, 'TN 333 7777', NULL);

INSERT INTO trips (id, driver_id, car_id, departure, arrival, departure_time, start_date, available_seats, price, luggage_policy, status) VALUES
(1, 2, 1, 'Tunis', 'Sousse', '08:30:00', '2026-05-25', 3, 15.0, 'SMALL', 'ACTIVE'),
(2, 2, 1, 'Sousse', 'Tunis', '17:00:00', '2026-05-25', 4, 15.0, 'SMALL', 'ACTIVE'),
(3, 3, 2, 'Tunis', 'Bizerte', '07:15:00', '2026-05-26', 3, 8.5, 'LARGE', 'ACTIVE'),
(4, 5, 3, 'Tunis', 'Sfax', '14:00:00', '2026-05-27', 2, 25.0, 'SMALL', 'ACTIVE'),
(5, 6, 4, 'Monastir', 'Sousse', '08:00:00', '2026-05-26', 3, 5.0, 'NONE', 'ACTIVE'),
(6, 9, 5, 'Tunis', 'Nabeul', '10:30:00', '2026-05-28', 4, 12.0, 'LARGE', 'ACTIVE'),
(7, 2, 1, 'Tunis', 'Sousse', '08:30:00', '2026-05-10', 0, 15.0, 'SMALL', 'COMPLETE'),
(8, 6, 4, 'Monastir', 'Sousse', '08:00:00', '2026-05-12', 0, 5.0, 'NONE', 'COMPLETE');

INSERT INTO requests (id, trip_id, passenger_id, quantity, total_cost, secret_code, status, luggage_size) VALUES
(1, 1, 4, 1, 15.0, 'A7B2', 'ACCEPTED', 'SMALL'),
(2, 1, 10, 1, 15.0, NULL, 'PENDING', 'SMALL'),
(3, 2, 8, 1, 15.0, 'G8H4', 'ACCEPTED', 'SMALL'),
(4, 4, 7, 1, 25.0, 'N2P8', 'ACCEPTED', 'LARGE'),
(5, 5, 8, 1, 5.0, 'S4T5', 'ACCEPTED', 'NONE'),
(6, 7, 4, 1, 15.0, 'DONE1', 'COMPLETE', 'SMALL'),
(7, 7, 8, 1, 15.0, 'DONE2', 'COMPLETE', 'SMALL'),
(8, 8, 10, 1, 5.0, 'DONE3', 'COMPLETE', 'NONE');

INSERT INTO ratings (trip_id, rater_id, rated_id, score, comment) VALUES
(7, 4, 2, 5, 'Excellent chauffeur.'),
(7, 8, 2, 5, 'Trajet agréable.'),
(7, 2, 4, 5, 'Passager poli.'),
(8, 10, 6, 5, 'Salma est top.');

INSERT INTO messages (request_id, sender_id, receiver_id, content, is_read) VALUES
(1, 4, 2, 'Bonjour, où est le RDV ?', 1),
(1, 2, 4, 'Avenue Habib Bourguiba.', 1),
(3, 8, 2, 'À 17h, c''est bon.', 0);

INSERT INTO notifications (user_id, title, message, is_read) VALUES
(4, 'Accepté', 'Trajet vers Sousse confirmé.', 1),
(2, 'Demande', 'Attia souhaite rejoindre votre trajet.', 1),
(10, 'Noter', 'N''oubliez pas de noter Salma !', 0);

INSERT INTO reports (reporter_id, reported_id, reason) VALUES
(7, 5, 'Fumait dans la voiture.'),
(11, 5, 'Annulation sans explication.');

INSERT INTO blocks (blocker_id, blocked_id) VALUES
(8, 5);

INSERT INTO badges (user_id, badge_name) VALUES
(2, 'CHAUFFEUR_FIABLE'),
(6, 'TOP_CONDUCTEUR'),
(8, 'PASSAGER_MODELE');

INSERT INTO saved_searches (passenger_id, name, departure, arrival, frequency) VALUES
(4, 'Tunis-Sousse', 'Tunis', 'Sousse', 'DAILY'),
(7, 'Tunis-Sfax', 'Tunis', 'Sfax', 'WEEKLY');

INSERT INTO stats (user_id, total_trips, money_saved, co2_saved, money_earned, cancelled_trips) VALUES
(2, 15, 0.0, 45.0, 225.0, 0),
(4, 5, 50.0, 12.0, 0.0, 1),
(6, 20, 0.0, 60.0, 100.0, 0),
(8, 12, 120.0, 30.0, 0.0, 0);

INSERT INTO otp_verifications (user_id, otp_code, expires_at, used) VALUES
(11, '123456', DATE_ADD(NOW(), INTERVAL 1 HOUR), 0),
(10, '654321', DATE_SUB(NOW(), INTERVAL 1 HOUR), 1);

INSERT INTO promo_codes (code, discount_pct, discount_fixed, max_uses, used_count, is_active) VALUES
('WELCOME20', 20.0, 0.0, 100, 10, 1),
('FIXED5', 0.0, 5.0, 50, 5, 1);

INSERT INTO driver_favorites (passenger_id, driver_id) VALUES
(8, 6), (4, 2);

INSERT INTO trip_chat_messages (trip_id, sender_id, content) VALUES
(1, 2, 'Bonjour tout le monde !'),
(1, 4, 'Bonjour !');

INSERT INTO disputes (trip_id, complainant_id, reason, status) VALUES
(4, 7, 'Supplément non prévu.', 'OPEN');

INSERT INTO payment_methods (id, user_id, type, last_four, brand, expiry) VALUES
(1, 2, 'Card', '5555', 'Visa', '12/28'),
(2, 6, 'VISA', '4444', 'Visa', '05/27'),
(3, 4, 'D17', '9987', 'D17', 'N/A'),
(4, 8, 'Card', '1234', 'Mastercard', '09/29');

INSERT INTO audit_log (actor_id, action, target_type, target_id, detail) VALUES
(1, 'BLOCK_USER',   'USER', 5,  'Karim Mansour — suite à 2 signalements'),
(1, 'APPROVE_ID',   'USER', 2,  'Youssef Ben Ali'),
(1, 'APPROVE_ID',   'USER', 3,  'Oumayma Trabelsi'),
(1, 'REJECT_ID',    'USER', 5,  'Document illisible'),
(1, 'UNBLOCK_USER', 'USER', 5,  'Karim Mansour — après appel');

SET FOREIGN_KEY_CHECKS = 1;
