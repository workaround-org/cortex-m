package de.u_project.cortex_m;

import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.CortexMToolProvider;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/mcp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpResource
{
	@Inject
	McpHttpConfigRepository mcpHttpConfigRepository;

	@Inject
	CortexMToolProvider mToolProvider;

	@GET
	public List<McpHttpConfig> getMcpConnections()
	{
		return mcpHttpConfigRepository.listAll();
	}

	@POST
	@Transactional
	public McpHttpConfig addMcpConnection(McpHttpConfig config)
	{
		mcpHttpConfigRepository.persist(config);
		mToolProvider.init();
		return config;
	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public void deleteMcpConnection(@PathParam("id") Long id)
	{
		mcpHttpConfigRepository.deleteById(id);
		mToolProvider.init();
	}
}
