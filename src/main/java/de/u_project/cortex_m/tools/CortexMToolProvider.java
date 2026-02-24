package de.u_project.cortex_m.tools;

import de.u_project.cortex_m.data.McpHttpConfig;
import de.u_project.cortex_m.data.McpHttpConfigRepository;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import io.quarkiverse.langchain4j.runtime.ToolsRecorder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class CortexMToolProvider implements ToolProvider
{
	private static final Logger log = LoggerFactory.getLogger(CortexMToolProvider.class);

	@Inject
	MemoryTool memoryTool;
	@Inject
	// Use Interface to inject
	McpConnectionTool mcpConnectionTool;

	@Inject
	McpHttpConfigRepository mcpConfigRepository;

	private Map<ToolSpecification, ToolExecutor> staticTools = new HashMap<>();
	private McpToolProvider mcpToolProvider;

	private static McpClient buildHttpClient(McpHttpConfig config)
	{
		try
		{
			StreamableHttpMcpTransport.Builder transportBuilder = StreamableHttpMcpTransport.builder()
				.url(config.getUrl());
			Optional.ofNullable(config.getAuthHeaderName())
				.filter(name -> !name.isBlank())
				.ifPresent(name -> transportBuilder.customHeaders(Map.of(name, config.getAuthHeaderValue())));
			McpTransport streamableHttpMcpTransport = transportBuilder.build();
			McpClient client = DefaultMcpClient.builder()
				.transport(streamableHttpMcpTransport)
				.key(config.getName())
				.clientName(config.getName())
				.build();
			log.info("Found {} tools for client {}", client.listTools().size(), config.getName());
			return client;
		}
		catch (Exception e)
		{
			log.error("Error creating MCP client for config {}: {}", config.getName(), e.getMessage());
			return null;
		}
	}

	@PostConstruct
	public void init()
	{
		staticTools = getToolConfig(List.of(memoryTool, mcpConnectionTool));
		List<McpClient> mcpClients = loadClientsFromDB();
		this.mcpToolProvider = McpToolProvider.builder()
			.mcpClients(mcpClients)
			.build();

	}

	@Override
	public ToolProviderResult provideTools(ToolProviderRequest request)
	{
		Map<ToolSpecification, ToolExecutor> mcpTools = new HashMap<>(mcpToolProvider.provideTools(request).tools());
		mcpTools.putAll(staticTools);
		return ToolProviderResult.builder()
			.addAll(mcpTools)
			.build();
	}

	@ActivateRequestContext
	protected List<McpClient> loadClientsFromDB()
	{
		return mcpConfigRepository.listAll().stream()
			.map(CortexMToolProvider::buildHttpClient)
			.filter(Objects::nonNull)
			.toList();
	}

	private Map<ToolSpecification, ToolExecutor> getToolConfig(List<Object> tools)
	{
		List<ToolSpecification> toolSpecs = new ArrayList<>();
		Map<String, ToolExecutor> executors = new HashMap<>();

		ToolsRecorder.populateToolMetadata(tools, toolSpecs, executors);

		Map<ToolSpecification, ToolExecutor> config = new HashMap<>();
		for (ToolSpecification specification : toolSpecs)
		{
			config.put(specification, executors.get(specification.name()));
		}
		return config;
	}
}
