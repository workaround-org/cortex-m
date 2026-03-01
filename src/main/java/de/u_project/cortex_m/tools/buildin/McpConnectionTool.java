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
public class McpConnectionTool implements CortexMTool
{
	@Inject
	McpHttpConfigRepository mcpHttpConfigRepository;

	@Inject
	CortexMToolProvider cortexMToolProvider;

	@Tool("Add a new MCP (Model Context Protocol) server connection. " +
		"'name' is a unique identifier for the connection. 'url' is the HTTP endpoint of the MCP server. " +
		"'authHeaderName' and 'authHeaderValue' are optional — set both to null if the server requires no authentication. " +
		"If 'authHeaderValue' is provided but 'authHeaderName' is null, 'Authorization' is used as the header name. " +
		"The new connection is activated immediately and its tools become available in the current session. " +
		"Returns a confirmation on success.")
	@Transactional
	public String addMcpConnection(String name, String url, String authHeaderName, String authHeaderValue)
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
		return "MCP connection '" + name + "' added.";
	}

	@Tool("List all configured MCP (Model Context Protocol) server connections. " +
		"Returns each connection's name, URL, and auth header name (the auth value is not shown for security reasons). " +
		"Use the name to reference a connection in update or remove operations.")
	public String listMcpConnections()
	{
		List<McpHttpConfig> configs = mcpHttpConfigRepository.listAll();
		if (configs.isEmpty())
		{
			return "No MCP connections configured.";
		}
		StringBuilder sb = new StringBuilder();
		for (McpHttpConfig c : configs)
		{
			sb.append("name=").append(c.getName())
				.append(", url=").append(c.getUrl())
				.append(", authHeaderName=").append(c.getAuthHeaderName() != null ? c.getAuthHeaderName() : "none")
				.append("\n");
		}
		return sb.toString().stripTrailing();
	}

	@Tool("Update the URL or authentication settings of an existing MCP server connection identified by 'name'. " +
		"'authHeaderName' and 'authHeaderValue' are optional — set both to null to remove authentication. " +
		"The connection is reloaded immediately after the update. " +
		"Returns a confirmation, or an error if no connection with that name exists.")
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

	@Tool("Remove an MCP server connection permanently by its 'name'. " +
		"The connection is deregistered immediately and its tools are no longer available. " +
		"Returns a confirmation, or an error if no connection with that name exists.")
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
