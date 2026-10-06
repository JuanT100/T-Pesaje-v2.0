using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using TPesaje.Application.Interfaces.Configuration;

namespace TPesaje.Api.Controllers.Configuration;

[ApiController]
[Route("api/configuration")]
[Authorize]
public sealed class ConfigurationController : ControllerBase
{
    private readonly ISystemSettingsRepository _repository;

    public ConfigurationController(
        ISystemSettingsRepository repository)
    {
        _repository = repository;
    }

    // Obtiene la configuración de sesión.
    [HttpGet("session")]
    public async Task<IActionResult> GetSessionConfiguration()
    {
        // Consulta el tiempo de inactividad configurado.
        var value =
            await _repository.GetValueAsync(
                "SessionInactivityMinutes");

        if (!int.TryParse(value, out var minutes))
        {
            return StatusCode(
                500,
                new
                {
                    success = false,
                    message = "Configuración de sesión no válida."
                });
        }

        return Ok(new
        {
            success = true,
            inactivityMinutes = minutes
        });
    }
}