namespace TPesaje.Application.DTOs.Authentication;

// Resultado de la autenticación.
public sealed class LoginResponse
{
    public bool Success { get; init; }

    public int? UserId { get; init; }

    public string? Username { get; init; }

    public string? FullName { get; init; }

    // Indica si el usuario debe cambiar su contraseña.
    public bool MustChangePassword { get; init; }

    // JWT generado para solicitudes autenticadas.
    public string? Token { get; init; }

    // Refresh Token utilizado para renovar el Access Token.
    public string? RefreshToken { get; init; }

    public string Message { get; init; } = string.Empty;
}
