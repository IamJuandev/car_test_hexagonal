-- Schema bootstrap for the Car management app (SQL Server 2022).
-- Idempotent: safe to run multiple times.

IF DB_ID('CarApp') IS NULL
    CREATE DATABASE CarApp;
GO

USE CarApp;
GO

IF OBJECT_ID('dbo.users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        name          VARCHAR(120) NOT NULL,
        email         VARCHAR(180) NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        created_at    DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT uq_users_email UNIQUE (email)
    );
END
GO

IF OBJECT_ID('dbo.cars', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cars (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id      BIGINT NOT NULL,
        brand        VARCHAR(80) NOT NULL,
        model        VARCHAR(80) NOT NULL,
        year         INT NOT NULL,
        plate_number VARCHAR(20) NOT NULL,
        color        VARCHAR(50) NOT NULL,
        photo_url    VARCHAR(255) NULL,
        created_at   DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at   DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_cars_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );

    -- A plate is unique per user (the same plate can't be duplicated by one owner).
    CREATE UNIQUE INDEX uq_cars_user_plate ON dbo.cars(user_id, plate_number);
    CREATE INDEX ix_cars_user ON dbo.cars(user_id);
END
GO
