package de.u_project.cortex_m.tools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import io.quarkiverse.langchain4j.runtime.ToolsRecorder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CortexMToolProvider implements ToolProvider
{
	@Inject
	MemoryTool memoryTool;

	private Map<ToolSpecification, ToolExecutor> staticTools = new HashMap<>();
	private McpToolProvider mcpToolProvider;

	@PostConstruct
	void init()
	{
		staticTools = getToolConfig(List.of(memoryTool));
	}

	public void setMcpToolProvider(McpToolProvider mcpToolProvider)
	{
		this.mcpToolProvider = mcpToolProvider;
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
