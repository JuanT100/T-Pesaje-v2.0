-- Crear la base de datos de v2.0 si no existe.
IF DB_ID('TPesajeDB_v2') IS NULL
BEGIN
    CREATE DATABASE TPesajeDB_v2;
END
GO

-- Seleccionar la base de datos de v2.0.
USE TPesajeDB_v2;
GO

-- Crear la tabla de usuarios si no existe.
IF OBJECT_ID('dbo.Users', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users
    (
        UserId INT IDENTITY(1,1) NOT NULL,
        Username VARCHAR(50) NOT NULL,
        FullName VARCHAR(100) NOT NULL,
        PasswordHash VARCHAR(255) NOT NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_Users_IsActive DEFAULT 1,
        FailedAttempts INT NOT NULL CONSTRAINT DF_Users_FailedAttempts DEFAULT 0,
        IsLocked BIT NOT NULL CONSTRAINT DF_Users_IsLocked DEFAULT 0,
        MustChangePassword BIT NOT NULL CONSTRAINT DF_Users_MustChangePassword DEFAULT 0,

        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NULL,

        CONSTRAINT PK_Users PRIMARY KEY (UserId),
        CONSTRAINT UQ_Users_Username UNIQUE (Username)
    );
END
GO


-- Seleccionar la base de datos de v2.0.
USE TPesajeDB_v2;
GO

-- Migrar los usuarios de v1.0 conservando sus hashes BCrypt.
INSERT INTO dbo.Users
(
    Username,
    FullName,
    PasswordHash,
    IsActive
)
SELECT
    Usuario,
    Nombre,
    Clave,
    Activo
FROM TPesajeDB.dbo.Usuarios;
GO


-- Verificar los usuarios migrados y el estado de seguridad.
SELECT
    UserId,
    Username,
    FullName,
    IsActive,
    FailedAttempts,
    IsLocked,
    MustChangePassword
FROM dbo.Users;


-- ============================================================
-- VERIFICAR Y CREAR LOGIN DE LA API
-- ============================================================

USE master;
GO

-- Crear el login únicamente si no existe.
IF NOT EXISTS (
    SELECT 1
    FROM sys.server_principals
    WHERE name = 'tpesaje_api_login'
)
BEGIN
    CREATE LOGIN [tpesaje_api_login]
    WITH PASSWORD = '<CONTRASEÑA_SEGURA>';
END
GO


-- ============================================================
-- VERIFICAR Y CREAR USUARIO DE LA API
-- ============================================================

USE TPesajeDB_v2;
GO

-- Crear el usuario únicamente si no existe.
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_principals
    WHERE name = 'tpesaje_api_user'
)
BEGIN
    CREATE USER [tpesaje_api_user]
    FOR LOGIN [tpesaje_api_login];
END
GO


-- ============================================================
-- VERIFICAR ASOCIACIÓN
-- ============================================================

USE TPesajeDB_v2;
GO

SELECT
    dp.name AS DatabaseUser,
    sp.name AS ServerLogin
FROM sys.database_principals dp
LEFT JOIN sys.server_principals sp
    ON dp.sid = sp.sid
WHERE dp.name = 'tpesaje_api_user';
GO


-- ============================================================
-- PERMISOS DE LA API
-- ============================================================

USE TPesajeDB_v2;
GO

-- Permisos sobre Users.
GRANT INSERT, SELECT, UPDATE
ON dbo.Users
TO [tpesaje_api_user];
GO


-- Verificar bloqueo de usuario de pruebas
USE TPesajeDB_V2;
GO

SELECT
    UserId,
    Username,
    FailedAttempts,
    IsLocked
FROM dbo.Users
WHERE Username = 'operador1';


-- Desbloquear usuario de prueba
USE TPesajeDB_V2;
GO

UPDATE dbo.Users
SET
    FailedAttempts = 0,
    IsLocked = 0,
    UpdatedAt = SYSUTCDATETIME()
WHERE Username = 'operador1';


-- Crear la tabla RefreshTokens
USE TPesajeDB_V2;
GO

IF OBJECT_ID('dbo.RefreshTokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.RefreshTokens
    (
        RefreshTokenId INT IDENTITY(1,1) NOT NULL,
        UserId INT NOT NULL,
        TokenHash VARCHAR(200) NOT NULL,
        ExpiresAt DATETIME2 NOT NULL,
        IsRevoked BIT NOT NULL CONSTRAINT DF_RefreshTokens_IsRevoked DEFAULT 0,
        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_RefreshTokens_CreatedAt DEFAULT SYSUTCDATETIME(),

        CONSTRAINT PK_RefreshTokens
            PRIMARY KEY (RefreshTokenId),

        CONSTRAINT UQ_RefreshTokens_TokenHash
            UNIQUE (TokenHash),

        CONSTRAINT FK_RefreshTokens_Users
            FOREIGN KEY (UserId)
            REFERENCES dbo.Users(UserId)
    );
END
GO

-- Permisos sobre RefreshTokens.
GRANT SELECT, INSERT, UPDATE
ON dbo.RefreshTokens
TO [tpesaje_api_user];
GO


-- Verificar Refresh Token del usuario de pruebas
USE TPesajeDB_v2;
GO

SELECT TOP 10
    RefreshTokenId,
    UserId,
    TokenHash,
    ExpiresAt,
    IsRevoked
FROM dbo.RefreshTokens
ORDER BY RefreshTokenId DESC;


-- Crear la tabla de configuraciones del sistema
CREATE TABLE dbo.SystemSettings
(
    SettingId INT IDENTITY(1,1) NOT NULL,
    SettingKey VARCHAR(100) NOT NULL,
    SettingValue VARCHAR(100) NOT NULL,
    Description VARCHAR(250) NULL,
    UpdatedAt DATETIME2 NOT NULL
        CONSTRAINT DF_SystemSettings_UpdatedAt
        DEFAULT SYSUTCDATETIME(),

    CONSTRAINT PK_SystemSettings
        PRIMARY KEY (SettingId),

    CONSTRAINT UQ_SystemSettings_SettingKey
        UNIQUE (SettingKey)
);
GO


-- Insertar el primer registro de inactividad por 15 minutos
INSERT INTO dbo.SystemSettings
(
    SettingKey,
    SettingValue,
    Description
)
VALUES
(
    'SessionInactivityMinutes',
    '15',
    'Tiempo de inactividad de la sesión en minutos.'
);
GO


-- Verificar Refresh Token del usuario de pruebas
USE TPesajeDB_v2;
GO

SELECT TOP 10
    RefreshTokenId,
    UserId,
    CreatedAt,
    ExpiresAt,
    IsRevoked
FROM dbo.RefreshTokens
WHERE UserId = (
    SELECT UserId
    FROM dbo.Users
    WHERE Username = 'operador1'
)
ORDER BY CreatedAt DESC;


-- Verificar IsRevoked = 1
USE TPesajeDB_v2;
GO

SELECT
    RefreshTokenId,
    UserId,
    CreatedAt,
    ExpiresAt,
    IsRevoked
FROM dbo.RefreshTokens
WHERE RefreshTokenId = 14;
