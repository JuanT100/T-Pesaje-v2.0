using TPesaje.Application.DTOs.Authentication;
using TPesaje.Application.Interfaces.Authentication;
using TPesaje.Application.Interfaces.Security;
using TPesaje.Domain.Entities.Authentication;

namespace TPesaje.Application.Services.Authentication;

// Ejecuta el proceso de autenticación del usuario.
public sealed class AuthenticationService : IAuthenticationService
{
    private const int MaxFailedAttempts = 3;

    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;
    private readonly ITokenService _tokenService;
    private readonly IRefreshTokenRepository _refreshTokenRepository;
    private readonly IRefreshTokenService _refreshTokenService;

    public AuthenticationService(
        IUserRepository userRepository,
        IPasswordHasher passwordHasher,
        ITokenService tokenService,
        IRefreshTokenRepository refreshTokenRepository,
        IRefreshTokenService refreshTokenService)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _tokenService = tokenService;
        _refreshTokenRepository = refreshTokenRepository;
        _refreshTokenService = refreshTokenService;
    }

    // Valida las credenciales y genera el JWT.
    public async Task<LoginResponse> LoginAsync(LoginRequest request)
    {
        var user = await _userRepository.GetByUsernameAsync(request.Username);

        // Verifica que el usuario exista.
        if (user is null)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "Usuario o contraseña incorrectos."
            };
        }

        // Verifica que el usuario esté activo.
        if (!user.IsActive)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "El usuario está inactivo."
            };
        }

        // Verifica que el usuario no esté bloqueado.
        if (user.IsLocked)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "El usuario está bloqueado."
            };
        }

        // Verifica la contraseña contra el hash almacenado.
        var validPassword =
            _passwordHasher.Verify(request.Password, user.PasswordHash);

        // Registra el intento fallido y aplica el bloqueo.
        if (!validPassword)
        {
            var failedAttempts = user.FailedAttempts + 1;
            var isLocked = failedAttempts >= MaxFailedAttempts;

            await _userRepository.UpdateSecurityStateAsync(
                user.UserId,
                failedAttempts,
                isLocked);

            return new LoginResponse
            {
                Success = false,
                Message = isLocked
                    ? "El usuario ha sido bloqueado."
                    : "Usuario o contraseña incorrectos."
            };
        }

        // Reinicia los intentos fallidos después de una autenticación correcta.
        await _userRepository.UpdateSecurityStateAsync(
            user.UserId,
            0,
            false);

        // Genera el JWT para el usuario autenticado.
        var token = _tokenService.GenerateToken(user);

        // Genera un Refresh Token para mantener la sesión.
        var refreshToken = _refreshTokenService.GenerateToken();

        // Crea la entidad que será almacenada en la base de datos.
        var refreshTokenEntity = new RefreshToken
        {
            UserId = user.UserId,
            Token = refreshToken,
            ExpiresAt = DateTime.UtcNow.AddDays(7),
            IsRevoked = false
        };

        // Guarda el Refresh Token asociado al usuario.
        await _refreshTokenRepository.SaveAsync(refreshTokenEntity);

        return new LoginResponse
        {
            Success = true,
            UserId = user.UserId,
            Username = user.Username,
            FullName = user.FullName,
            MustChangePassword = user.MustChangePassword,
            Token = token,
            RefreshToken = refreshToken,
            Message = "Autenticación correcta."
        };
    }

    // Renueva el Access Token y rota el Refresh Token.
    public async Task<LoginResponse> RefreshTokenAsync(string refreshToken)
    {
        // Busca el Refresh Token almacenado.
        var storedRefreshToken =
            await _refreshTokenRepository.GetByTokenAsync(refreshToken);

        // Verifica que el Refresh Token exista, no esté revocado y no haya expirado.
        if (storedRefreshToken is null ||
            storedRefreshToken.IsRevoked ||
            storedRefreshToken.ExpiresAt <= DateTime.UtcNow)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "No se pudo renovar la sesión."
            };
        }

        // Obtiene el usuario asociado al Refresh Token.
        var user =
            await _userRepository.GetByIdAsync(storedRefreshToken.UserId);

        // Verifica que el usuario exista, esté activo y no esté bloqueado.
        if (user is null || !user.IsActive || user.IsLocked)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "No se pudo renovar la sesión."
            };
        }

        // Revoca el Refresh Token utilizado.
        var revoked =
            await _refreshTokenRepository.RevokeAsync(
                storedRefreshToken.RefreshTokenId);

        // Evita reutilizar un Refresh Token que ya fue utilizado.
        if (!revoked)
        {
            return new LoginResponse
            {
                Success = false,
                Message = "No se pudo renovar la sesión."
            };
        }

        // Genera un nuevo Access Token.
        var token = _tokenService.GenerateToken(user);

        // Genera un nuevo Refresh Token.
        var newRefreshToken = _refreshTokenService.GenerateToken();

        // Crea la entidad correspondiente al nuevo Refresh Token.
        var newRefreshTokenEntity = new RefreshToken
        {
            UserId = user.UserId,
            Token = newRefreshToken,
            ExpiresAt = DateTime.UtcNow.AddDays(7),
            IsRevoked = false
        };

        // Guarda el nuevo Refresh Token asociado al usuario.
        await _refreshTokenRepository.SaveAsync(newRefreshTokenEntity);

        return new LoginResponse
        {
            Success = true,
            UserId = user.UserId,
            Username = user.Username,
            FullName = user.FullName,
            MustChangePassword = user.MustChangePassword,
            Token = token,
            RefreshToken = newRefreshToken,
            Message = "Access Token renovado correctamente."
        };
    }

    // Revoca el Refresh Token de la sesión.
    public async Task<bool> LogoutAsync(string refreshToken)
    {
        if (string.IsNullOrWhiteSpace(refreshToken))
            return false;

        var storedToken =
            await _refreshTokenRepository.GetByTokenAsync(
                refreshToken);

        if (storedToken is null)
            return false;

        return await _refreshTokenRepository.RevokeAsync(
            storedToken.RefreshTokenId);
    }
}
