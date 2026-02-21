package de.u_project.cortex_m;

import de.u_project.cortex_m.bot.CortexMBot;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/chat")
public class ChatResource
{
	@Inject
	CortexMBot cortexMBot;

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String chat(String message)
	{
		return cortexMBot.chat(message);
	}
}
