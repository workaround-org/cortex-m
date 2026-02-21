package de.u_project.cortex_m;

import de.u_project.cortex_m.data.McpHttpConfig;
import de.u_project.cortex_m.data.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.CortexMToolProvider;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
