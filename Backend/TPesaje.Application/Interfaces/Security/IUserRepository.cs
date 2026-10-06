using TPesaje.Domain.Entities.Security;

namespace TPesaje.Application.Interfaces.Security;

// Define las operaciones de persistencia de usuarios.
public interface IUserRepository
{
    // Obtiene un usuario por su nombre de usuario.
    Task<User?> GetByUsernameAsync(string username);

    // Actualiza el estado de seguridad del usuario.
    Task UpdateSecurityStateAsync(
        int userId,
        int failedAttempts,
        bool isLocked);

    // Obtiene un usuario por su identificador.
    Task<User?> GetByIdAsync(int userId);
}
