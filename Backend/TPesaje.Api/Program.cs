using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using TPesaje.Application.Interfaces.Authentication;
using TPesaje.Application.Interfaces.Configuration;
using TPesaje.Application.Interfaces.Security;
using TPesaje.Application.Services.Authentication;
using TPesaje.Infrastructure.Persistence;
using TPesaje.Infrastructure.Repositories;
using TPesaje.Infrastructure.Repositories.Security;
using TPesaje.Infrastructure.Security;

var builder = WebApplication.CreateBuilder(args);

// Obtiene la cadena de conexión desde la configuración.
var connectionString =
    builder.Configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException(
        "DefaultConnection no está configurada.");

// Obtiene la clave utilizada para firmar los JWT.
var jwtKey =
    builder.Configuration["Jwt:Key"]
    ?? throw new InvalidOperationException(
        "Jwt:Key no está configurada.");

// Obtiene la duración del Access Token.
var jwtExpiration =
    builder.Configuration.GetValue<int>("Jwt:ExpirationMinutes");

builder.Services.AddControllers();

// Registra el repositorio de usuarios.
builder.Services.AddScoped<IUserRepository>(
    _ => new UserRepository(connectionString));

// Registra el repositorio de Refresh Tokens.
builder.Services.AddScoped<IRefreshTokenRepository>(
    _ => new RefreshTokenRepository(connectionString));

// Registra el repositorio de configuración del sistema.
builder.Services.AddScoped<ISystemSettingsRepository>(
    _ => new SystemSettingsRepository(connectionString));

// Registra el servicio de contraseñas.
builder.Services.AddScoped<IPasswordHasher, PasswordHasher>();

// Registra el servicio de autenticación.
builder.Services.AddScoped<IAuthenticationService, AuthenticationService>();

// Registra el servicio que genera los JWT.
builder.Services.AddScoped<ITokenService>(
    _ => new JwtTokenService(jwtKey, jwtExpiration));

// Registra el servicio que genera Refresh Tokens.
builder.Services.AddScoped<IRefreshTokenService, RefreshTokenService>();

// Configura la autenticación mediante JWT Bearer.
builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            // Valida la firma del JWT.
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(jwtKey)),

            // Por ahora no se validan Issuer ni Audience.
            ValidateIssuer = false,
            ValidateAudience = false,

            // Valida la fecha de expiración del token.
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero
        };
    });

// Habilita la autorización para los endpoints protegidos.
builder.Services.AddAuthorization();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Habilita Swagger únicamente en desarrollo.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

// Autentica las solicitudes mediante JWT.
app.UseAuthentication();

// Autoriza las solicitudes autenticadas.
app.UseAuthorization();

app.MapControllers();

app.Run();