using System.ComponentModel.DataAnnotations;

namespace TPesaje.Application.DTOs.Authentication;

// Datos recibidos para iniciar sesión.
public sealed class LoginRequest
{
    [Required]
    [StringLength(50)]
    public string Username { get; init; } = string.Empty;

    [Required]
    public string Password { get; init; } = string.Empty;
}
