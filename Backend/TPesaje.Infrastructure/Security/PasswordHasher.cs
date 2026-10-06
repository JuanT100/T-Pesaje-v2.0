using TPesaje.Application.Interfaces.Authentication;

namespace TPesaje.Infrastructure.Security;

// Implementa la protección y verificación de contraseñas mediante BCrypt.
public sealed class PasswordHasher : IPasswordHasher
{
    // Genera el hash de una contraseña.
    public string Hash(string password)
    {
        return BCrypt.Net.BCrypt.HashPassword(password);
    }

    // Verifica una contraseña contra su hash almacenado.
    public bool Verify(string password, string passwordHash)
    {
        return BCrypt.Net.BCrypt.Verify(password, passwordHash);
    }
}