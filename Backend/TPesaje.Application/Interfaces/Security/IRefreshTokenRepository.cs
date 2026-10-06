using TPesaje.Domain.Entities.Authentication;

namespace TPesaje.Application.Interfaces.Security;

// Define las operaciones de persistencia de Refresh Tokens.
public interface IRefreshTokenRepository
{
    // Guarda un Refresh Token asociado a un usuario.
    Task SaveAsync(RefreshToken refreshToken);

    // Obtiene un Refresh Token por su valor.
    Task<RefreshToken?> GetByTokenAsync(string token);

    // Revoca un Refresh Token que ya fue utilizado.
    Task<bool> RevokeAsync(int refreshTokenId);
}