using TPesaje.Application.DTOs.Authentication;

namespace TPesaje.Application.Interfaces.Authentication;

// Define el caso de uso de autenticación.
public interface IAuthenticationService
{
    // Ejecuta el proceso de inicio de sesión.
    Task<LoginResponse> LoginAsync(LoginRequest request);

    // Renueva el Access Token utilizando un Refresh Token válido.
    Task<LoginResponse> RefreshTokenAsync(string refreshToken);

    // Cierra la sesión y revoca el Refresh Token.
    Task<bool> LogoutAsync(string refreshToken);
}
