package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.memory.MemoryIngestor;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MemoryTool
{
	@Inject
	MemoryIngestor memoryIngestor;

	@Tool
	public void saveToMemory(String data)
	{
		memoryIngestor.ingest(data);
	}

	@Tool
	public String queryMemory(String query)
	{
		return memoryIngestor.augment(query).toString();
	}
}
