package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.CortexMToolProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class McpConnectionTool
{
	@Inject
	McpHttpConfigRepository mcpHttpConfigRepository;

	@Inject
	CortexMToolProvider cortexMToolProvider;

	@Tool("Add MCP Connection. authHeaderValue is optional, If not used set to null.")
	@Transactional
	public void addMcpConnection(String name, String url, String authHeaderName, String authHeaderValue)
	{
		boolean emptyHeaderValue = authHeaderValue == null || authHeaderValue.isBlank();
		boolean emptyHeaderName = authHeaderName == null || authHeaderName.isBlank();
		if (emptyHeaderValue)
		{
			authHeaderName = null;
			authHeaderValue = null;
		}
		// If authHeaderValue is provided but authHeaderName is not, default to "Authorization"
		else if (emptyHeaderName)
		{
			authHeaderName = "Authorization";
		}
		McpHttpConfig config = new McpHttpConfig();
		config.setName(name);
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		mcpHttpConfigRepository.persist(config);
		cortexMToolProvider.init();
	}

	@Tool("List all MCP Connections. Returns name, URL and auth header name for each connection.")
	public List<McpHttpConfig> listMcpConnections()
	{
		List<McpHttpConfig> configs = mcpHttpConfigRepository.listAll();
		if (configs.isEmpty())
		{
			return List.of();
		}
		return configs;
	}

	@Tool("Update an existing MCP Connection by name. authHeaderName and authHeaderValue are optional, set to null to remove auth.")
	@Transactional
	public String updateMcpConnection(String name, String url, String authHeaderName, String authHeaderValue)
	{
		McpHttpConfig config = mcpHttpConfigRepository.find("name", name).firstResult();
		if (config == null)
		{
			return "No MCP connection found with name: " + name;
		}
		boolean emptyHeaderValue = authHeaderValue == null || authHeaderValue.isBlank();
		boolean emptyHeaderName = authHeaderName == null || authHeaderName.isBlank();
		if (emptyHeaderValue)
		{
			authHeaderName = null;
			authHeaderValue = null;
		}
		else if (emptyHeaderName)
		{
			authHeaderName = "Authorization";
		}
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		mcpHttpConfigRepository.persist(config);
		cortexMToolProvider.init();
		return "MCP connection '" + name + "' updated.";
	}

	@Tool("Remove an MCP Connection by name.")
	@Transactional
	public String removeMcpConnection(String name)
	{
		long deleted = mcpHttpConfigRepository.delete("name", name);
		if (deleted == 0)
		{
			return "No MCP connection found with name: " + name;
		}
		cortexMToolProvider.init();
		return "MCP connection '" + name + "' removed.";
	}
}
