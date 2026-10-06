namespace TPesaje.Application.DTOs.Authentication;

// Contiene el Refresh Token enviado para solicitar un nuevo Access Token.
public sealed class RefreshTokenRequest
{
    public string RefreshToken { get; init; } = string.Empty;
}