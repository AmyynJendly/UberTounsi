-- ============================================================
-- CovoitDark — Database Schema
-- Run: mysql -u root -p1234 < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS covoiturage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE covoiturage;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS payment_methods;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
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
    id_verified     BOOLEAN DEFAULT FALSE,
    cancellation_rate DOUBLE  DEFAULT 0.0,
    id_document     TEXT DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Cars
CREATE TABLE IF NOT EXISTS cars (
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

-- 3. Trips
CREATE TABLE IF NOT EXISTS trips (
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

-- 4. Requests (Reservations)
CREATE TABLE IF NOT EXISTS requests (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    trip_id         INT NOT NULL,
    passenger_id    INT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    total_cost      DOUBLE NOT NULL DEFAULT 0.0,
    secret_code     VARCHAR(10),
    status          ENUM('PENDING','ACCEPTED','REJECTED','WAITLIST','CANCELLED','COMPLETE','NEGOTIATING') DEFAULT 'PENDING',
    luggage_size    ENUM('NONE','SMALL','LARGE') DEFAULT 'SMALL',
    counter_price   DOUBLE DEFAULT NULL,
    promo_code      VARCHAR(30) DEFAULT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Ratings
CREATE TABLE IF NOT EXISTS ratings (
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

-- 6. Messages
CREATE TABLE IF NOT EXISTS messages (
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

-- 7. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    message         TEXT NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 8. Reports
CREATE TABLE IF NOT EXISTS reports (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    reporter_id     INT NOT NULL,
    reported_id     INT NOT NULL,
    reason          TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (reported_id) REFERENCES users(id)
);

-- 9. Blocks
CREATE TABLE IF NOT EXISTS blocks (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    blocker_id      INT NOT NULL,
    blocked_id      INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_block (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users(id),
    FOREIGN KEY (blocked_id) REFERENCES users(id)
);

-- 10. Badges
CREATE TABLE IF NOT EXISTS badges (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    badge_name      VARCHAR(100) NOT NULL,
    awarded_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 11. Saved Searches
CREATE TABLE IF NOT EXISTS saved_searches (
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

-- 12. Stats
CREATE TABLE IF NOT EXISTS stats (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT UNIQUE NOT NULL,
    total_trips     INT DEFAULT 0,
    money_saved     DOUBLE DEFAULT 0.0,
    co2_saved       DOUBLE DEFAULT 0.0,
    money_earned    DOUBLE DEFAULT 0.0,
    cancelled_trips INT    DEFAULT 0,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. OTP Verifications (Feature 4 – Phone/Email OTP)
CREATE TABLE IF NOT EXISTS otp_verifications (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NOT NULL,
    otp_code    VARCHAR(6) NOT NULL,
    expires_at  DATETIME NOT NULL,
    used        BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 14. Promo Codes (Feature 12)
CREATE TABLE IF NOT EXISTS promo_codes (
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

-- 15. Driver Favorites (Feature 7)
CREATE TABLE IF NOT EXISTS driver_favorites (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    driver_id    INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_fav (passenger_id, driver_id),
    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id)    REFERENCES users(id) ON DELETE CASCADE
);

-- 16. Trip Group Chat (Feature 3)
CREATE TABLE IF NOT EXISTS trip_chat_messages (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    trip_id     INT NOT NULL,
    sender_id   INT NOT NULL,
    content     TEXT NOT NULL,
    sent_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id)   REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 17. Disputes (Feature 13)
CREATE TABLE IF NOT EXISTS disputes (
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

-- 18. Audit Log
CREATE TABLE IF NOT EXISTS audit_log (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    actor_id    INT NOT NULL,
    action      VARCHAR(60) NOT NULL,
    target_type VARCHAR(40),
    target_id   INT,
    detail      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 19. Payment Methods
CREATE TABLE IF NOT EXISTS payment_methods (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT NOT NULL,
    type            VARCHAR(50) NOT NULL, -- 'Card', 'D17', 'VISA', etc.
    last_four       VARCHAR(10),
    brand           VARCHAR(50), -- 'Visa', 'Mastercard', 'D17', etc.
    expiry          VARCHAR(10), -- 'MM/YY' or 'N/A'
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Default admin account (password: Admin1234!)
-- Hash will be set by AuthController.createDefaultAdmin() on first run
INSERT IGNORE INTO users (full_name, email, password, salt, role, is_verified)
VALUES ('Administrateur', 'admin@covoitdark.tn', 'CHANGEME', 'CHANGEME', 'ADMIN', TRUE);

-- ============================================================
-- Performance Indexes
-- ============================================================
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
