using Microsoft.Data.SqlClient;
using TPesaje.Application.Interfaces.Security;
using TPesaje.Domain.Entities.Security;

namespace TPesaje.Infrastructure.Repositories;

// Implementa el acceso a datos de los usuarios.
public sealed class UserRepository : IUserRepository
{
    private readonly string _connectionString;

    public UserRepository(string connectionString)
    {
        _connectionString = connectionString;
    }

    // Obtiene un usuario por su nombre de usuario.
    public async Task<User?> GetByUsernameAsync(string username)
    {
        const string sql = """
            SELECT
                UserId,
                Username,
                FullName,
                PasswordHash,
                IsActive,
                FailedAttempts,
                IsLocked,
                MustChangePassword,
                CreatedAt,
                UpdatedAt
            FROM dbo.Users
            WHERE Username = @Username;
            """;

        await using var connection = new SqlConnection(_connectionString);
        await using var command = new SqlCommand(sql, connection);

        // Agrega el parámetro para evitar SQL Injection.
        command.Parameters.AddWithValue("@Username", username);

        await connection.OpenAsync();

        await using var reader = await command.ExecuteReaderAsync();

        if (!await reader.ReadAsync())
        {
            return null;
        }

        return new User
        {
            UserId = reader.GetInt32(reader.GetOrdinal("UserId")),
            Username = reader.GetString(reader.GetOrdinal("Username")),
            FullName = reader.GetString(reader.GetOrdinal("FullName")),
            PasswordHash = reader.GetString(reader.GetOrdinal("PasswordHash")),
            IsActive = reader.GetBoolean(reader.GetOrdinal("IsActive")),
            FailedAttempts = reader.GetInt32(reader.GetOrdinal("FailedAttempts")),
            IsLocked = reader.GetBoolean(reader.GetOrdinal("IsLocked")),
            MustChangePassword = reader.GetBoolean(reader.GetOrdinal("MustChangePassword")),
            CreatedAt = reader.GetDateTime(reader.GetOrdinal("CreatedAt")),
            UpdatedAt = reader.IsDBNull(reader.GetOrdinal("UpdatedAt"))
                ? null
                : reader.GetDateTime(reader.GetOrdinal("UpdatedAt"))
        };
    }

    // Actualiza los intentos fallidos y el estado de bloqueo.
    public async Task UpdateSecurityStateAsync(
        int userId,
        int failedAttempts,
        bool isLocked)
    {
        const string sql = """
            UPDATE dbo.Users
            SET
                FailedAttempts = @FailedAttempts,
                IsLocked = @IsLocked,
                UpdatedAt = SYSUTCDATETIME()
            WHERE UserId = @UserId;
            """;

        await using var connection = new SqlConnection(_connectionString);
        await using var command = new SqlCommand(sql, connection);

        // Agrega los parámetros para evitar SQL Injection.
        command.Parameters.AddWithValue("@UserId", userId);
        command.Parameters.AddWithValue("@FailedAttempts", failedAttempts);
        command.Parameters.AddWithValue("@IsLocked", isLocked);

        await connection.OpenAsync();

        await command.ExecuteNonQueryAsync();
    }

    // Obtiene un usuario por su identificador.
    public async Task<User?> GetByIdAsync(int userId)
    {
        const string sql = """
        SELECT
            UserId,
            Username,
            FullName,
            PasswordHash,
            IsActive,
            FailedAttempts,
            IsLocked,
            MustChangePassword,
            CreatedAt,
            UpdatedAt
        FROM dbo.Users
        WHERE UserId = @UserId;
        """;

        await using var connection = new SqlConnection(_connectionString);
        await connection.OpenAsync();

        await using var command = new SqlCommand(sql, connection);

        // Agrega el parámetro para evitar SQL Injection.
        command.Parameters.AddWithValue("@UserId", userId);

        await using var reader = await command.ExecuteReaderAsync();

        // El usuario no existe.
        if (!await reader.ReadAsync())
        {
            return null;
        }

        return new User
        {
            UserId = reader.GetInt32(reader.GetOrdinal("UserId")),
            Username = reader.GetString(reader.GetOrdinal("Username")),
            FullName = reader.GetString(reader.GetOrdinal("FullName")),
            PasswordHash = reader.GetString(reader.GetOrdinal("PasswordHash")),
            IsActive = reader.GetBoolean(reader.GetOrdinal("IsActive")),
            FailedAttempts = reader.GetInt32(reader.GetOrdinal("FailedAttempts")),
            IsLocked = reader.GetBoolean(reader.GetOrdinal("IsLocked")),
            MustChangePassword = reader.GetBoolean(reader.GetOrdinal("MustChangePassword")),
            CreatedAt = reader.GetDateTime(reader.GetOrdinal("CreatedAt")),
            UpdatedAt = reader.IsDBNull(reader.GetOrdinal("UpdatedAt"))
                ? null
                : reader.GetDateTime(reader.GetOrdinal("UpdatedAt"))
        };
    }

}