-- Demo data for the PostgreSQL deployment. Mirrors database/seed.sql.
--
-- Demo account:
--   email:    demo@carapp.test
--   password: password123
-- The hash is a real BCrypt hash ($2b$, cost 10) that verifies with the
-- BCryptPasswordEncoder behind BCryptPasswordHasherAdapter.
--
-- Idempotent: ON CONFLICT DO NOTHING makes every restart a no-op.

INSERT INTO users (name, email, password_hash)
VALUES ('Demo User', 'demo@carapp.test',
        '$2b$10$pEn1k1quAcC0Cw5jo.GfieeJOjN6Pi47uTGUjgFBIQoRxijFLQJ9C')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cars (user_id, brand, model, year, plate_number, color, photo_url)
SELECT u.id, 'Toyota', 'Corolla', 2021, 'ABC123', 'Silver', NULL
FROM users u
WHERE u.email = 'demo@carapp.test'
ON CONFLICT (user_id, plate_number) DO NOTHING;

INSERT INTO cars (user_id, brand, model, year, plate_number, color, photo_url)
SELECT u.id, 'Ford', 'Focus', 2019, 'XYZ789', 'Blue', NULL
FROM users u
WHERE u.email = 'demo@carapp.test'
ON CONFLICT (user_id, plate_number) DO NOTHING;
