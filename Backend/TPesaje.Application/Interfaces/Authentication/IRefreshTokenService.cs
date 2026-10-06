namespace TPesaje.Application.Interfaces.Authentication;

// Define la generación de Refresh Tokens.
public interface IRefreshTokenService
{
    // Genera un nuevo Refresh Token.
    string GenerateToken();
}