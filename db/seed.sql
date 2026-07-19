-- ============================================================
-- CovoitDark — Seed Data  (Password for all demo users: Covoit2026!)
-- This file contains INSERT statements ONLY.
-- Table structure is managed exclusively by schema.sql.
-- Run after schema.sql: mysql -u root -p covoiturage < db/seed.sql
-- ============================================================

USE covoiturage;

SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data to allow clean re-seeding
TRUNCATE TABLE audit_log;
TRUNCATE TABLE disputes;
TRUNCATE TABLE trip_chat_messages;
TRUNCATE TABLE driver_favorites;
TRUNCATE TABLE promo_codes;
TRUNCATE TABLE otp_verifications;
TRUNCATE TABLE payment_methods;
TRUNCATE TABLE saved_searches;
TRUNCATE TABLE badges;
TRUNCATE TABLE blocks;
TRUNCATE TABLE reports;
TRUNCATE TABLE ratings;
TRUNCATE TABLE notifications;
TRUNCATE TABLE messages;
TRUNCATE TABLE requests;
TRUNCATE TABLE stats;
TRUNCATE TABLE trips;
TRUNCATE TABLE cars;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ── Users (Password for all: Covoit2026!) ──────────────────────────────────
INSERT INTO users (id, full_name, email, password, salt, phone, role, avatar, bio, languages, reputation_score, is_verified, failed_logins, is_blocked, balance) VALUES
(1, 'Amine Admin',    'amine@covoit.tn',   '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 20 100 001', 'ADMIN',     'no-avatar', 'Administrateur du système CovoitDark. Toujours disponible.',          'Français, Arabe, Anglais',    5.00, 1, 0, 0, 100.0),
(2, 'Youssef Driver', 'youssef@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 22 345 678', 'DRIVER',    'no-avatar', 'Chauffeur expérimenté, ponctuel et courtois. Plus de 10 ans de route.', 'Arabe, Français',             4.80, 1, 0, 0,  50.0),
(3, 'Oumayma Driver', 'oumayma@driver.tn', '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 25 987 321', 'DRIVER',    'no-avatar', 'Voyageuse passionnée, conduite douce. Trajets femmes uniquement.',     'Français, Anglais, Arabe',    4.90, 1, 0, 0,  65.0),
(4, 'Attia Passager', 'attia@pass.tn',     '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 23 654 987', 'PASSENGER', 'no-avatar', 'Étudiant à la faculté des sciences de Tunis. Trajets quotidiens.',     'Arabe, Français',             4.50, 1, 0, 0, 120.0),
(5, 'Karim Chauffeur','karim@driver.tn',   '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 94 112 233', 'DRIVER',    'no-avatar', 'Bonne humeur sur la route. Musique, ambiance, sécurité avant tout.',   'Arabe, Italien, Français',    4.20, 1, 0, 0,  15.0),
(6, 'Salma Chauffeur','salma@driver.tn',   '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 55 778 899', 'DRIVER',    'no-avatar', 'Conduite calme et professionnelle. Ce trajet tous les jours depuis 2 ans.', 'Français, Arabe',           4.95, 1, 0, 0,  75.0),
(7, 'Wael Passager',  'wael@pass.tn',      '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 50 321 456', 'PASSENGER', 'no-avatar', 'Ingénieur informatique. Cherche trajets bon marché et confortables.',  'Arabe, Anglais',              3.80, 1, 0, 0,  80.0),
(8, 'Mouna Passager', 'mouna@pass.tn',     '6f3c585ec915c398c3719c695d7d795761a3b67b7745ac589e7031cd5b62cbf9', '6RSkIeCLwCdKxjpy3CWvC72R9bYBLyel/zzJuj8L4vA=', '+216 98 456 123', 'PASSENGER', 'no-avatar', 'Professeure universitaire. Aime voyager confortablement.',             'Anglais, Français, Arabe',    5.00, 1, 0, 0, 350.0);

-- ── Stats ───────────────────────────────────────────────────────────────────
INSERT INTO stats (user_id, total_trips, money_saved, co2_saved) VALUES
(1,  0,   0.0,   0.0),
(2, 12, 185.0,  32.5),
(3,  7,  95.0,  18.2),
(4,  3,  25.0,   5.5),
(5,  5,  45.0,  10.0),
(6, 20, 420.0,  65.0),
(7,  1,   5.0,   1.2),
(8,  8, 120.0,  22.0);

-- ── Cars ────────────────────────────────────────────────────────────────────
INSERT INTO cars (id, driver_id, brand, model, color, seats, license_plate, image_base64) VALUES
(1, 2, 'Volkswagen', 'Golf 7',   'Noir',            4, 'TN-202-5555', 'no-image'),
(2, 3, 'Peugeot',   '3008',     'Gris Métallique',  4, 'TN-150-1234', 'no-image'),
(3, 5, 'Renault',   'Clio 4',   'Rouge Passion',    4, 'TN-195-9876', 'no-image'),
(4, 6, 'Kia',       'Rio',      'Blanc Nacré',      3, 'TN-210-4444', 'no-image'),
(5, 2, 'BMW',       'Série 3',  'Bleu Saphir',      4, 'TN-180-1111', 'no-image');

-- ── Trips ───────────────────────────────────────────────────────────────────
INSERT INTO trips (id, driver_id, car_id, departure, arrival, departure_time, start_date, end_date, repeat_days, available_seats, price, status, luggage_policy, music_preference, smoking_allowed, ac_available, flexible_pickup, women_only, pets_allowed) VALUES
(1, 2, 1, 'Tunis',    'Sousse',   '08:30:00', '2026-05-20', '2026-12-31', 'Lundi,Mercredi',                         4, 15.0, 'ACTIVE', 'SMALL', 'ANY',  0, 1, 1, 0, 0),
(2, 2, 1, 'Sousse',   'Tunis',    '17:00:00', '2026-05-20', '2026-12-31', 'Lundi,Mercredi,Jeudi',                   4, 15.0, 'ACTIVE', 'SMALL', 'ANY',  0, 1, 0, 0, 0),
(3, 3, 2, 'Ariana',   'Bizerte',  '07:15:00', '2026-05-21', '2026-12-31', 'Vendredi,Samedi',                        3,  8.5, 'ACTIVE', 'LARGE', 'SOFT', 0, 1, 0, 1, 1),
(4, 5, 3, 'Tunis',    'Sfax',     '14:00:00', '2026-06-01', '2026-06-30', 'Lundi,Mercredi,Vendredi',                4, 25.0, 'ACTIVE', 'SMALL', 'ANY',  1, 1, 1, 0, 0),
(5, 5, 3, 'Sfax',     'Tunis',    '09:00:00', '2026-06-03', '2026-06-30', 'Mardi,Jeudi,Samedi',                     4, 25.0, 'ACTIVE', 'SMALL', 'ANY',  1, 1, 1, 0, 0),
(6, 6, 4, 'Monastir', 'Sousse',   '08:00:00', '2026-05-22', '2026-12-31', 'Lundi,Mardi,Mercredi,Jeudi,Vendredi',   3,  3.5, 'ACTIVE', 'NONE',  'SOFT', 0, 1, 1, 1, 0),
(7, 6, 4, 'Sousse',   'Monastir', '18:00:00', '2026-05-22', '2026-12-31', 'Lundi,Mardi,Mercredi,Jeudi,Vendredi',   3,  3.5, 'ACTIVE', 'NONE',  'SOFT', 0, 1, 1, 1, 0),
(8, 2, 5, 'Tunis',    'Nabeul',   '10:30:00', '2026-07-15', '2026-08-31', 'Samedi,Dimanche',                        4, 12.0, 'ACTIVE', 'LARGE', 'ANY',  0, 1, 0, 0, 1);

-- ── Requests ────────────────────────────────────────────────────────────────
INSERT INTO requests (trip_id, passenger_id, quantity, total_cost, secret_code, status) VALUES
(1, 4, 1, 15.0, 'V8N1', 'ACCEPTED'),
(2, 8, 1, 15.0, 'R5C9', 'ACCEPTED'),
(3, 7, 1,  8.5, 'L2D6', 'PENDING'),
(4, 7, 2, 50.0, 'K3M4', 'PENDING'),
(5, 8, 1, 25.0, 'H6J3', 'ACCEPTED'),
(6, 8, 1,  3.5, 'X9P2', 'ACCEPTED'),
(6, 4, 1,  3.5, 'T7Q5', 'REJECTED'),
(8, 4, 2, 24.0, 'W4F8', 'WAITLIST');

-- ── Messages ────────────────────────────────────────────────────────────────
INSERT INTO messages (request_id, sender_id, receiver_id, content, is_read, is_quick_response, sent_at) VALUES
(1, 4, 2, 'Bonjour Youssef, ma réservation est confirmée, merci beaucoup !',                 1, 1, '2026-05-20 07:00:00'),
(1, 2, 4, 'Pas de problème Attia, à demain matin à 8h30 !',                                  1, 1, '2026-05-20 07:15:00'),
(2, 8, 2, 'Bonjour, j''ai bien reçu le code. Je serai là à l''heure.',                      1, 0, '2026-05-20 16:45:00'),
(2, 2, 8, 'Parfait Mouna, à tout à l''heure !',                                              1, 1, '2026-05-20 16:50:00'),
(6, 8, 6, 'Bonjour Salma, je serai à l''arrêt demain matin à 8h00. À demain !',             1, 0, '2026-05-21 07:45:00'),
(6, 6, 8, 'Parfait Mouna, je serai là. Bonne soirée !',                                     1, 0, '2026-05-21 08:10:00'),
(4, 7, 5, 'Bonjour Karim, est-ce que le trajet du lundi est toujours confirmé ?',           1, 0, '2026-05-30 18:30:00'),
(4, 5, 7, 'Oui Wael, on part à 14h précises depuis la gare routière de Tunis.',             1, 0, '2026-05-30 19:00:00');

-- ── Ratings ─────────────────────────────────────────────────────────────────
INSERT INTO ratings (trip_id, rater_id, rated_id, score, comment, created_at) VALUES
(1, 4, 2, 5, 'Youssef est un chauffeur exemplaire. Très ponctuel, conduite douce. Je recommande !',    '2026-05-20 12:00:00'),
(1, 2, 4, 4, 'Attia est un passager très poli et respectueux. Aucun problème.',                         '2026-05-20 12:05:00'),
(2, 8, 2, 5, 'Excellent retour depuis Sousse. Youssef est de très bonne compagnie.',                    '2026-05-20 20:00:00'),
(2, 2, 8, 5, 'Passagère parfaite, toujours à l''heure. Je la recommande à tous les chauffeurs.',        '2026-05-20 20:05:00'),
(6, 8, 6, 5, 'Salma conduit parfaitement. Voiture propre, musique douce, très agréable. 5 étoiles !',  '2026-05-22 09:30:00'),
(6, 6, 8, 5, 'Mouna est une passagère idéale, ponctuelle et très sympathique.',                         '2026-05-22 09:35:00'),
(5, 8, 5, 3, 'Le trajet était correct mais la voiture sentait la cigarette. Un peu décevant.',          '2026-06-03 13:00:00'),
(5, 5, 8, 4, 'Bonne passagère, à l''heure et discrète. Merci Mouna.',                                  '2026-06-03 13:10:00');

-- ── Reports ─────────────────────────────────────────────────────────────────
INSERT INTO reports (reporter_id, reported_id, reason, created_at) VALUES
(7, 5, 'Karim n''est pas venu au point de rendez-vous et n''a pas répondu à mes appels. Très peu professionnel.', '2026-06-02 15:00:00'),
(4, 5, 'Il a annulé le trajet sans prévenir à la dernière minute, ce qui m''a mis en retard au travail.',         '2026-06-02 16:30:00');

-- ── Blocks ──────────────────────────────────────────────────────────────────
INSERT INTO blocks (blocker_id, blocked_id, created_at) VALUES
(8, 5, '2026-06-03 14:00:00'),
(4, 5, '2026-06-02 17:00:00');

-- ── Badges ──────────────────────────────────────────────────────────────────
INSERT INTO badges (user_id, badge_name, awarded_at) VALUES
(1, 'Administrateur',   '2025-12-01 00:00:00'),
(2, 'Chauffeur Fiable', '2026-03-01 10:00:00'),
(2, 'Top Conducteur',   '2026-04-15 10:00:00'),
(3, 'Chauffeur Fiable', '2026-04-01 09:00:00'),
(3, 'Trajets Écolos',   '2026-03-15 09:00:00'),
(4, 'Passager Modèle',  '2026-05-01 11:00:00'),
(6, 'Chauffeur Fiable', '2026-01-10 08:00:00'),
(6, 'Top Conducteur',   '2026-02-20 08:00:00'),
(6, 'Trajets Écolos',   '2026-03-05 08:00:00'),
(8, 'Passager Modèle',  '2026-04-10 11:00:00');

-- ── Saved Searches ──────────────────────────────────────────────────────────
INSERT INTO saved_searches (passenger_id, name, departure, arrival, preferred_time, frequency, start_date, end_date, created_at) VALUES
(4, 'Mon trajet quotidien', 'Tunis',  'Sousse', '08:00:00', 'Quotidien',    '2026-05-01', '2026-12-31', '2026-04-20 10:00:00'),
(7, 'Tunis - Sfax hebdo',   'Tunis',  'Sfax',   '14:00:00', 'Hebdomadaire', '2026-06-01', '2026-09-30', '2026-04-21 09:00:00'),
(8, 'Retour Sousse soir',   'Sousse', 'Tunis',  '17:00:00', 'Quotidien',    '2026-05-01', '2026-12-31', '2026-04-19 14:00:00'),
(4, 'Weekend Nabeul',       'Tunis',  'Nabeul', '10:00:00', 'Weekend',      '2026-07-01', '2026-08-31', '2026-04-22 08:00:00');

-- ── Notifications ───────────────────────────────────────────────────────────
INSERT INTO notifications (user_id, title, message, is_read, sent_at) VALUES
(4, 'Réservation confirmée !', 'Votre réservation Tunis → Sousse avec Youssef Driver est acceptée. Code : V8N1',       1, '2026-05-20 07:30:00'),
(8, 'Réservation confirmée !', 'Votre réservation Monastir → Sousse avec Salma Chauffeur est acceptée. Code : X9P2',   1, '2026-05-21 08:00:00'),
(8, 'Réservation confirmée !', 'Votre réservation Sousse → Tunis avec Youssef Driver est acceptée. Code : R5C9',       1, '2026-05-20 17:00:00'),
(8, 'Réservation confirmée !', 'Votre réservation Sfax → Tunis avec Karim Chauffeur est acceptée. Code : H6J3',        0, '2026-06-03 09:30:00'),
(7, 'Demande en attente',      'Votre demande Tunis → Sfax est en attente de confirmation du chauffeur.',              0, '2026-05-30 12:00:00'),
(7, 'Demande en attente',      'Votre demande Ariana → Bizerte est en attente de confirmation du chauffeur.',          1, '2026-05-29 10:00:00'),
(4, 'Demande rejetée',         'Votre demande pour Monastir → Sousse a été refusée. Essayez un autre trajet.',         1, '2026-05-21 09:00:00'),
(4, 'Liste d''attente',        'Votre demande Tunis → Nabeul est en liste d''attente. Vous serez notifié bientôt.',    0, '2026-06-10 10:00:00'),
(2, 'Nouveau passager !',      'Attia Passager a réservé une place sur votre trajet Tunis → Sousse du 2026-05-20.',    1, '2026-05-19 22:00:00'),
(2, 'Nouveau passager !',      'Mouna Passager a réservé une place sur votre trajet Sousse → Tunis du 2026-05-20.',    1, '2026-05-20 16:00:00'),
(6, 'Nouveau passager !',      'Mouna Passager a réservé une place sur votre trajet Monastir → Sousse du 2026-05-22.', 1, '2026-05-20 18:00:00'),
(5, 'Nouveau signalement',     'Vous avez reçu un signalement. Notre équipe examinera la situation sous 48h.',         0, '2026-06-02 16:00:00'),
(8, 'Nouveau message',         'Vous avez un nouveau message de Salma Chauffeur.',                                     1, '2026-05-21 08:12:00'),
(7, 'Nouveau message',         'Vous avez un nouveau message de Karim Chauffeur concernant votre trajet.',             1, '2026-05-30 19:02:00');

-- ── Payment Methods ─────────────────────────────────────────────────────────
INSERT INTO payment_methods (id, user_id, type, last_four, brand, expiry) VALUES
(1, 1, 'Card', '4242', 'Visa',       '12/28'),
(2, 2, 'D17',  '5678', 'D17',        'N/A'),
(3, 4, 'Card', '1111', 'Mastercard', '05/27'),
(4, 8, 'VISA', '8888', 'Visa',       '09/29');
