namespace TPesaje.Domain.Entities.Authentication;

// Representa un Refresh Token asociado a un usuario.
public sealed class RefreshToken
{
    public int RefreshTokenId { get; init; }

    public int UserId { get; init; }

    // Valor real del Refresh Token utilizado por el cliente.
    public string Token { get; init; } = string.Empty;

    // Fecha y hora de expiración del Refresh Token.
    public DateTime ExpiresAt { get; init; }

    // Indica si el token fue revocado.
    public bool IsRevoked { get; init; }
}