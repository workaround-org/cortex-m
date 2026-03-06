package de.u_project.cortex_m;

import de.u_project.cortex_m.bot.CortexMService;
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
	CortexMService cortexMBot;

	@Inject
	MemoryIngestor memoryIngestor;

	@Inject
	ConnectorWS connectorWS;

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String chat(String message)
	{
		return cortexMBot.chat(message, "default-memory");
	}

	@POST
	@Path("broadcast")
	public void broadcast(String message)
	{
		connectorWS.broadCast(message);
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
