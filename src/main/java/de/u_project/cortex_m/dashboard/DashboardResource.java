package de.u_project.cortex_m.dashboard;

import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.List;

@Path("/dashboard")
public class DashboardResource
{
	@Inject
	Template dashboard;
	@Inject
	Template soul;
	@Inject
	Template scheduledTask;

	@Inject
	McpHttpConfigRepository mcpRepository;
	@Inject
	ScheduledTaskRepository taskRepository;
	@Inject
	CortexMSoulRepository soulRepository;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response redirect()
	{
		URI uri = UriBuilder.fromPath("/dashboard/mcp-configs").build();
		return Response.temporaryRedirect(uri).build();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("mcp-configs")
	public TemplateInstance getMcpConfigs()
	{
		List<McpHttpConfig> configs = mcpRepository.listAll();
		return dashboard.data("configs", configs);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("tasks")
	public TemplateInstance getTasks()
	{
		List<ScheduledTask> tasks = taskRepository.listAll();
		return scheduledTask.data("tasks", tasks);
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("souls")
	public TemplateInstance getSouls()
	{
		List<CortexMSoul> souls = soulRepository.listAll();
		return soul.data("souls", souls);
	}
}
