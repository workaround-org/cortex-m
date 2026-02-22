package de.u_project.cortex_m;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.memory.MemoryIngestor;
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

	@Inject
	MemoryIngestor memoryIngestor;

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String chat(String message)
	{
		return cortexMBot.chat(message);
	}

	@POST
	@Path("memory")
	// Demo endpoint
	public void ingestMemory(String data)
	{
		memoryIngestor.ingest(data);
	}

	@POST
	@Path("memory/augment")
	// Demo endpoint
	public String augmentMemory(String query)
	{
		return memoryIngestor.augment(query);
	}
}
