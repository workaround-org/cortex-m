package de.u_project.cortex_m.tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MemoryTool
{
	@Tool
	public void saveToMemory(String data)
	{
		System.out.println("Saving to memory: " + data);
	}
}
