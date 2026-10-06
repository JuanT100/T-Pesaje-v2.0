using Microsoft.Data.SqlClient;
using TPesaje.Application.Interfaces.Security;
using TPesaje.Domain.Entities.Authentication;

namespace TPesaje.Infrastructure.Repositories.Security;

// Implementa el almacenamiento de Refresh Tokens.
public sealed class RefreshTokenRepository : IRefreshTokenRepository
{
    private readonly string _connectionString;

    public RefreshTokenRepository(string connectionString)
    {
        _connectionString = connectionString;
    }

    // Guarda un Refresh Token asociado a un usuario.
    public async Task SaveAsync(RefreshToken refreshToken)
    {
        const string sql = """
        INSERT INTO dbo.RefreshTokens
        (
            UserId,
            TokenHash,
            ExpiresAt,
            IsRevoked
        )
        VALUES
        (
            @UserId,
            @TokenHash,
            @ExpiresAt,
            @IsRevoked
        );
        """;

        var tokenHash = Convert.ToHexString(
            System.Security.Cryptography.SHA256.HashData(
                System.Text.Encoding.UTF8.GetBytes(refreshToken.Token)));

        await using var connection = new SqlConnection(_connectionString);
        await using var command = new SqlCommand(sql, connection);

        // Agrega los parámetros para evitar SQL Injection.
        command.Parameters.AddWithValue("@UserId", refreshToken.UserId);
        command.Parameters.AddWithValue("@TokenHash", tokenHash);
        command.Parameters.AddWithValue("@ExpiresAt", refreshToken.ExpiresAt);
        command.Parameters.AddWithValue("@IsRevoked", refreshToken.IsRevoked);

        await connection.OpenAsync();

        await command.ExecuteNonQueryAsync();
    }

    // Obtiene un Refresh Token por su valor.
    public async Task<RefreshToken?> GetByTokenAsync(string token)
    {
        const string sql = """
        SELECT
            RefreshTokenId,
            UserId,
            TokenHash,
            ExpiresAt,
            IsRevoked
        FROM dbo.RefreshTokens
        WHERE TokenHash = @TokenHash;
        """;

        var tokenHash = Convert.ToHexString(
            System.Security.Cryptography.SHA256.HashData(
                System.Text.Encoding.UTF8.GetBytes(token)));

        await using var connection = new SqlConnection(_connectionString);
        await using var command = new SqlCommand(sql, connection);

        // Agrega el hash del token para evitar almacenar o consultar el valor original.
        command.Parameters.AddWithValue("@TokenHash", tokenHash);

        await connection.OpenAsync();

        await using var reader = await command.ExecuteReaderAsync();

        // El Refresh Token no existe.
        if (!await reader.ReadAsync())
        {
            return null;
        }

        return new RefreshToken
        {
            RefreshTokenId = reader.GetInt32(
                reader.GetOrdinal("RefreshTokenId")),

            UserId = reader.GetInt32(
                reader.GetOrdinal("UserId")),

            Token = tokenHash,

            ExpiresAt = reader.GetDateTime(
                reader.GetOrdinal("ExpiresAt")),

            IsRevoked = reader.GetBoolean(
                reader.GetOrdinal("IsRevoked"))
        };
    }

    // Revoca un Refresh Token que ya fue utilizado.
    public async Task<bool> RevokeAsync(int refreshTokenId)
    {
        const string sql = """
        UPDATE dbo.RefreshTokens
        SET IsRevoked = 1
        WHERE RefreshTokenId = @RefreshTokenId
          AND IsRevoked = 0;
        """;

        await using var connection = new SqlConnection(_connectionString);
        await using var command = new SqlCommand(sql, connection);

        command.Parameters.AddWithValue(
            "@RefreshTokenId",
            refreshTokenId);

        await connection.OpenAsync();

        var rowsAffected = await command.ExecuteNonQueryAsync();

        // True indica que el token estaba vigente y fue revocado.
        return rowsAffected > 0;
    }

}