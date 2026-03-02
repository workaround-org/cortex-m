package de.u_project.cortex_m.dashboard;

import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
	@Inject
	TaskBean taskBean;

	@Context
	UriInfo uriInfo;

	// ── Redirect ─────────────────────────────────────────────────────────────

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response redirect()
	{
		return seeOther("mcp-configs");
	}

	// ── MCP Configs ───────────────────────────────────────────────────────────

	@GET
	@Path("mcp-configs")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance getMcpConfigs()
	{
		return dashboard
			.data("configs", mcpRepository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Path("mcp-configs")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response createMcpConfig(
		@FormParam("name") String name,
		@FormParam("url") String url,
		@FormParam("authHeaderName") String authHeaderName,
		@FormParam("authHeaderValue") String authHeaderValue)
	{
		McpHttpConfig config = new McpHttpConfig();
		config.setName(name);
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		mcpRepository.persist(config);
		return seeOther("mcp-configs");
	}

	@POST
	@Path("mcp-configs/{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response updateMcpConfig(
		@PathParam("id") Long id,
		@FormParam("name") String name,
		@FormParam("url") String url,
		@FormParam("authHeaderName") String authHeaderName,
		@FormParam("authHeaderValue") String authHeaderValue)
	{
		McpHttpConfig config = mcpRepository.findById(id);
		if (config == null) return Response.status(Response.Status.NOT_FOUND).build();
		config.setName(name);
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		return seeOther("mcp-configs");
	}

	@POST
	@Path("mcp-configs/{id}/delete")
	@Transactional
	public Response deleteMcpConfig(@PathParam("id") Long id)
	{
		mcpRepository.deleteById(id);
		return seeOther("mcp-configs");
	}

	// ── Souls ─────────────────────────────────────────────────────────────────

	@GET
	@Path("souls")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance getSouls()
	{
		return soul
			.data("souls", soulRepository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Path("souls")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response createSoul(@FormParam("text") String text)
	{
		CortexMSoul s = new CortexMSoul();
		s.setText(text);
		soulRepository.persist(s);
		return seeOther("souls");
	}

	@POST
	@Path("souls/{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response updateSoul(@PathParam("id") Long id, @FormParam("text") String text)
	{
		CortexMSoul s = soulRepository.findById(id);
		if (s == null) return Response.status(Response.Status.NOT_FOUND).build();
		s.setText(text);
		return seeOther("souls");
	}

	@POST
	@Path("souls/{id}/delete")
	@Transactional
	public Response deleteSoul(@PathParam("id") Long id)
	{
		soulRepository.deleteById(id);
		return seeOther("souls");
	}

	// ── Tasks ─────────────────────────────────────────────────────────────────

	@GET
	@Path("tasks")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance getTasks()
	{
		return scheduledTask
			.data("tasks", taskRepository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Path("tasks")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createTask(
		@FormParam("taskType") String taskType,
		@FormParam("prompt") String prompt,
		@FormParam("executeAt") String executeAt,
		@FormParam("cronExpression") String cronExpression,
		@FormParam("startAt") String startAt)
	{
		try
		{
			if ("CRON".equals(taskType))
			{
				Instant start = parseInstant(startAt, Instant.now());
				taskBean.addCronTrigger(prompt, new RecurringSchedule(cronExpression), start);
			}
			else
			{
				taskBean.addTrigger(prompt, parseInstant(executeAt, Instant.now()));
			}
		}
		catch (Exception e)
		{
			// TODO: surface validation errors in UI
		}
		return seeOther("tasks");
	}

	@POST
	@Path("tasks/{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateTask(
		@PathParam("id") Long id,
		@FormParam("taskType") String taskType,
		@FormParam("prompt") String prompt,
		@FormParam("executeAt") String executeAt,
		@FormParam("cronExpression") String cronExpression,
		@FormParam("startAt") String startAt)
	{
		try
		{
			taskBean.deleteTask(id);
			if ("CRON".equals(taskType))
			{
				Instant start = parseInstant(startAt, Instant.now());
				taskBean.addCronTrigger(prompt, new RecurringSchedule(cronExpression), start);
			}
			else
			{
				taskBean.addTrigger(prompt, parseInstant(executeAt, Instant.now()));
			}
		}
		catch (Exception e)
		{
			// TODO: surface validation errors in UI
		}
		return seeOther("tasks");
	}

	@POST
	@Path("tasks/{id}/delete")
	public Response deleteTask(@PathParam("id") Long id)
	{
		try { taskBean.deleteTask(id); } catch (Exception ignored) {}
		return seeOther("tasks");
	}

	// ── Helpers ───────────────────────────────────────────────────────────────

	private Response seeOther(String subPath)
	{
		return Response.seeOther(
			uriInfo.getBaseUriBuilder().path("dashboard").path(subPath).build()
		).build();
	}

	private static Instant parseInstant(String datetimeLocal, Instant fallback)
	{
		if (datetimeLocal == null || datetimeLocal.isBlank()) return fallback;
		return LocalDateTime.parse(datetimeLocal).toInstant(ZoneOffset.UTC);
	}
}

