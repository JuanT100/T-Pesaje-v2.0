using Microsoft.Data.SqlClient;
using TPesaje.Application.Interfaces.Configuration;

namespace TPesaje.Infrastructure.Persistence;

// Consulta los parámetros almacenados en SQL Server.
public sealed class SystemSettingsRepository : ISystemSettingsRepository
{
    private readonly string _connectionString;

    public SystemSettingsRepository(string connectionString)
    {
        _connectionString = connectionString;
    }

    // Obtiene el valor de una configuración.
    public async Task<string?> GetValueAsync(string key)
    {
        const string sql = """
            SELECT SettingValue
            FROM dbo.SystemSettings
            WHERE SettingKey = @Key;
            """;

        await using var connection =
            new SqlConnection(_connectionString);

        await connection.OpenAsync();

        await using var command =
            new SqlCommand(sql, connection);

        command.Parameters.AddWithValue("@Key", key);

        var result =
            await command.ExecuteScalarAsync();

        return result?.ToString();
    }
}