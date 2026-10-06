using System.Security.Cryptography;
using TPesaje.Application.Interfaces.Authentication;

namespace TPesaje.Infrastructure.Security;

// Genera Refresh Tokens criptográficamente seguros.
public sealed class RefreshTokenService : IRefreshTokenService
{
    // Genera un token aleatorio de alta entropía.
    public string GenerateToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(64);

        return Convert.ToBase64String(bytes);
    }
}