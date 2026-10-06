using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using TPesaje.Application.Interfaces.Authentication;
using TPesaje.Domain.Entities.Security;

namespace TPesaje.Infrastructure.Security;

// Genera tokens JWT para usuarios autenticados.
public sealed class JwtTokenService : ITokenService
{
    private readonly string _key;
    private readonly int _expirationMinutes;

    public JwtTokenService(string key, int expirationMinutes)
    {
        _key = key;
        _expirationMinutes = expirationMinutes;
    }

    // Genera un JWT con la identidad básica del usuario.
    public string GenerateToken(User user)
    {
        var claims = new[]
        {
            // Identificador del usuario.
            new Claim(JwtRegisteredClaimNames.Sub, user.UserId.ToString()),

            // Nombre de usuario.
            new Claim(JwtRegisteredClaimNames.UniqueName, user.Username),

            // Nombre completo.
            new Claim("fullName", user.FullName)
        };

        // Crea la clave de firma a partir de la clave configurada.
        var securityKey = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(_key));

        // Define el algoritmo utilizado para firmar el token.
        var credentials = new SigningCredentials(
            securityKey,
            SecurityAlgorithms.HmacSha256);

        // Crea el JWT con sus claims, expiración y firma.
        var token = new JwtSecurityToken(
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(_expirationMinutes),
            signingCredentials: credentials);

        // Serializa el JWT para enviarlo al cliente.
        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}