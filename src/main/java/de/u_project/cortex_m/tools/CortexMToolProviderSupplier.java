package de.u_project.cortex_m.tools;

import dev.langchain4j.service.tool.ToolProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.function.Supplier;

@Singleton
public class CortexMToolProviderSupplier implements Supplier<ToolProvider>
{
	@Inject
	CortexMToolProvider cortexMToolProvider;

	@Override
	public ToolProvider get()
	{
		return cortexMToolProvider;
	}
}
