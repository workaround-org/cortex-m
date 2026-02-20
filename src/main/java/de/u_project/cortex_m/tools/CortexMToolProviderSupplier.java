package de.u_project.cortex_m.tools;

import de.u_project.cortex_m.data.McpHttpConfig;
import de.u_project.cortex_m.data.McpHttpConfigRepository;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

@Singleton
public class CortexMToolProviderSupplier implements Supplier<ToolProvider>
{
	private static final Logger log = LoggerFactory.getLogger(CortexMToolProviderSupplier.class);

	@Inject
	McpHttpConfigRepository mcpConfigRepository;

	private McpToolProvider mcpToolProvider;

	private static McpClient buildHttpClient(McpHttpConfig config)
	{
		McpTransport streamableHttpMcpTransport = StreamableHttpMcpTransport.builder()
			.url(config.getUrl())
			.build();
		McpClient client = DefaultMcpClient.builder()
			.transport(streamableHttpMcpTransport)
			.key(config.getName())
			.clientName(config.getName())
			.build();
		log.info("Found {} tools for client {}", client.listTools().size(), config.getName());
		return client;
	}

	/**
	 * Call this to update the MCP clients from the database. This is not automatically called, because we want to control when the update happens (e.g. after a new client is added to the database).
	 */
	@PostConstruct
	public void init()
	{
		List<McpClient> mcpClients = loadClientsFromDB();
		// Challenge: How to add static tools? extend McpToolProvider / use a router?
		mcpToolProvider = McpToolProvider.builder()
			.mcpClients(mcpClients)
			.build();
	}

	@ActivateRequestContext
	protected List<McpClient> loadClientsFromDB()
	{
		return mcpConfigRepository.listAll().stream()
			.map(CortexMToolProviderSupplier::buildHttpClient)
			.toList();
	}

	@Override
	public ToolProvider get()
	{
		return mcpToolProvider;
	}
}
