package de.u_project.cortex_m;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.tools.CortexMToolProviderSupplier;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/cortex-m")
public class ChatResource
{
	@Inject
	CortexMBot cortexMBot;

	@Inject
	CortexMToolProviderSupplier cortexMToolProviderSupplier;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello()
	{
		return "Hello from Quarkus REST";
	}

	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	public String updateMCP()
	{
		cortexMToolProviderSupplier.init();
		return "Hello from Quarkus REST";
	}

	@POST
	@Path("/chat")
	@Produces(MediaType.TEXT_PLAIN)
	public String chat(String message)
	{
		return cortexMBot.chat(message);
	}
}
