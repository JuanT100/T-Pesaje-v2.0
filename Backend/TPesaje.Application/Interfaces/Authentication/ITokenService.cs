using TPesaje.Domain.Entities.Security;

namespace TPesaje.Application.Interfaces.Authentication;

// Define la generación de tokens JWT.
public interface ITokenService
{
    // Genera un JWT para un usuario autenticado.
    string GenerateToken(User user);
}
