namespace TPesaje.Application.Interfaces.Configuration;

// Define la consulta de configuración del sistema.
public interface ISystemSettingsRepository
{
    // Obtiene el valor de una configuración.
    Task<string?> GetValueAsync(string key);
}