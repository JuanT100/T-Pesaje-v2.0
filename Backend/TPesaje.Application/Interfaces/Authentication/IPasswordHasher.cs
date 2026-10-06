namespace TPesaje.Application.Interfaces.Authentication;

// Define las operaciones para proteger y verificar contraseñas.
public interface IPasswordHasher
{
    // Genera el hash de una contraseña.
    string Hash(string password);

    // Verifica una contraseña contra su hash.
    bool Verify(string password, string passwordHash);
}
