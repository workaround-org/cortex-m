package de.u_project.cortex_m.dashboard;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard")
public class DashboardResource extends DashboardSupport
{
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response redirect()
	{
		return seeOther("mcp-configs");
	}
}