package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.memory.MemoryIngestor;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MemoryTool implements CortexMTool
{
	@Inject
	MemoryIngestor memoryIngestor;

	@Tool("Store a piece of information in long-term semantic memory. " +
		"'data' should be a self-contained, meaningful text snippet (e.g. a fact, preference, or note about the user). " +
		"Use this to remember things that may be relevant in future conversations. " +
		"Returns a confirmation once the data has been ingested.")
	public String saveToMemory(String data)
	{
		memoryIngestor.ingest(data);
		return "Stored in memory: " + data;
	}

	@Tool("Search long-term semantic memory for information relevant to a query. " +
		"'query' should describe what you are looking for (e.g. 'user food preferences', 'upcoming birthdays'). " +
		"Returns the most relevant memory segments found, or an empty result if nothing matches.")
	public String queryMemory(String query)
	{
		return memoryIngestor.augment(query);
	}
}
