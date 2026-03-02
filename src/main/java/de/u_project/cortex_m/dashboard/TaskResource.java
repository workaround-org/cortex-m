package de.u_project.cortex_m.dashboard;

import de.u_project.cortex_m.database.ScheduledTaskRepository;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;

@Path("/dashboard/tasks")
public class TaskResource extends DashboardSupport
{
	@Inject
	Template scheduledTask;

	@Inject
	ScheduledTaskRepository repository;

	@Inject
	TaskBean taskBean;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance list()
	{
		return scheduledTask
			.data("tasks", repository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(
		@FormParam("taskType") String taskType,
		@FormParam("prompt") String prompt,
		@FormParam("executeAt") String executeAt,
		@FormParam("cronExpression") String cronExpression,
		@FormParam("startAt") String startAt)
	{
		try
		{
			scheduleTask(taskType, prompt, executeAt, cronExpression, startAt);
		}
		catch (Exception e)
		{
			// TODO: surface validation errors in UI
		}
		return seeOther("tasks");
	}

	@POST
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(
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
			scheduleTask(taskType, prompt, executeAt, cronExpression, startAt);
		}
		catch (Exception e)
		{
			// TODO: surface validation errors in UI
		}
		return seeOther("tasks");
	}

	@POST
	@Path("{id}/delete")
	public Response delete(@PathParam("id") Long id)
	{
		try { taskBean.deleteTask(id); } catch (Exception ignored) {}
		return seeOther("tasks");
	}

	private void scheduleTask(String taskType, String prompt, String executeAt,
		String cronExpression, String startAt) throws Exception
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
}
