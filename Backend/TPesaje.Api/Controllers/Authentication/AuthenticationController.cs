using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using TPesaje.Application.DTOs.Authentication;
using TPesaje.Application.Interfaces.Authentication;

namespace TPesaje.Api.Controllers.Authentication;

// Expone los endpoints relacionados con la autenticación.
[ApiController]
[Route("api/auth")]
public sealed class AuthenticationController : ControllerBase
{
    private readonly IAuthenticationService _authenticationService;

    // Recibe el servicio de autenticación mediante inyección de dependencias.
    public AuthenticationController(
        IAuthenticationService authenticationService)
    {
        _authenticationService = authenticationService;
    }

    // Permite iniciar sesión sin estar autenticado.
    [AllowAnonymous]
    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login(
        [FromBody] LoginRequest request)
    {
        // Ejecuta la validación de credenciales y genera el JWT.
        var result = await _authenticationService.LoginAsync(request);

        // Devuelve 401 si la autenticación no fue exitosa.
        if (!result.Success)
        {
            return Unauthorized(result);
        }

        // Devuelve 200 con los datos de autenticación y el JWT.
        return Ok(result);
    }

    // Permite renovar el Access Token utilizando un Refresh Token válido.
    [AllowAnonymous]
    [HttpPost("refresh")]
    public async Task<ActionResult<LoginResponse>> Refresh(
        [FromBody] RefreshTokenRequest request)
    {
        // Ejecuta el proceso de renovación.
        var result =
            await _authenticationService.RefreshTokenAsync(request.RefreshToken);

        // Devuelve 401 si la renovación no fue posible.
        if (!result.Success)
        {
            return Unauthorized(result);
        }

        // Devuelve 200 con el nuevo Access Token.
        return Ok(result);
    }

    [Authorize]
    [HttpPost("logout")]
    public async Task<IActionResult> Logout(
    [FromBody] LogoutRequest request)
    {
        var revoked =
        await _authenticationService.LogoutAsync(
            request.RefreshToken);

        if (!revoked)
        {
            return Unauthorized(new
            {
                success = false,
                message = "No se pudo cerrar la sesión."
            });
        }

        return Ok(new
        {
            success = true,
            message = "Sesión cerrada correctamente."
        });
    }
}