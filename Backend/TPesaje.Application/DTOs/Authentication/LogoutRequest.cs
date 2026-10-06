namespace TPesaje.Application.DTOs.Authentication;

// Contiene el Refresh Token de la sesión.
public sealed class LogoutRequest
{
    public string RefreshToken { get; init; } = string.Empty;
}