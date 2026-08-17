-- Demo data for the Car management app (SQL Server 2022).
-- Idempotent: safe to run multiple times; applied by container-up.sh right after init.sql.
--
-- Demo account:
--   email:    demo@carapp.test
--   password: password123
-- The password_hash below is a real BCrypt hash ($2b$, cost 10) that verifies
-- with Spring's BCryptPasswordEncoder used in BCryptPasswordHasherAdapter.

USE CarApp;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo@carapp.test')
BEGIN
    INSERT INTO dbo.users (name, email, password_hash)
    VALUES ('Demo User', 'demo@carapp.test',
            '$2b$10$pEn1k1quAcC0Cw5jo.GfieeJOjN6Pi47uTGUjgFBIQoRxijFLQJ9C');
END
GO

DECLARE @demo_id BIGINT = (SELECT id FROM dbo.users WHERE email = 'demo@carapp.test');

IF NOT EXISTS (SELECT 1 FROM dbo.cars WHERE user_id = @demo_id AND plate_number = 'ABC123')
    INSERT INTO dbo.cars (user_id, brand, model, year, plate_number, color, photo_url)
    VALUES (@demo_id, 'Toyota', 'Corolla', 2021, 'ABC123', 'Silver', NULL);

IF NOT EXISTS (SELECT 1 FROM dbo.cars WHERE user_id = @demo_id AND plate_number = 'XYZ789')
    INSERT INTO dbo.cars (user_id, brand, model, year, plate_number, color, photo_url)
    VALUES (@demo_id, 'Ford', 'Focus', 2019, 'XYZ789', 'Blue', NULL);
GO
