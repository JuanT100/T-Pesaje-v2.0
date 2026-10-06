namespace TPesaje.Domain.Entities.Security
{
    // Representa un usuario del sistema.
    public sealed class User
    {
        public int UserId { get; init; }

        public string Username { get; init; } = string.Empty;

        public string FullName { get; init; } = string.Empty;

        // Almacena únicamente el hash de la contraseña.
        public string PasswordHash { get; init; } = string.Empty;

        public bool IsActive { get; init; }

        // Controla los intentos fallidos de autenticación.
        public int FailedAttempts { get; init; }

        // Indica si el usuario está bloqueado.
        public bool IsLocked { get; init; }

        // Indica si debe cambiar su contraseña.
        public bool MustChangePassword { get; init; }

        public DateTime CreatedAt { get; init; }

        public DateTime? UpdatedAt { get; init; }

    }
}
