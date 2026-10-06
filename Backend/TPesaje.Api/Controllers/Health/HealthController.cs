using Microsoft.AspNetCore.Mvc;

namespace TPesaje.Api.Controllers;

// Proporciona un endpoint para verificar la disponibilidad del Backend.
[ApiController]
[Route("health")]
public sealed class HealthController : ControllerBase
{
    // Verifica que el Backend esté disponible.
    [HttpGet]
    public IActionResult Get()
    {
        return Ok(new
        {
            Status = "Healthy"
        });
    }
}
