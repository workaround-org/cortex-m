package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.CortexMToolProvider;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

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
		// If authHeaderValue is provided but authHeaderName is not, default to "Authorization
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
}
