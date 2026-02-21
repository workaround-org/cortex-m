package de.u_project.cortex_m.tools;

import de.u_project.cortex_m.data.McpHttpConfig;
import de.u_project.cortex_m.data.McpHttpConfigRepository;
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

	@Tool
	@Transactional
	public void addMcpConnection(String name, String url)
	{
		McpHttpConfig config = new McpHttpConfig();
		config.setName(name);
		config.setUrl(url);
		mcpHttpConfigRepository.persist(config);
		cortexMToolProvider.init();
	}
}
