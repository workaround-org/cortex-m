package de.u_project.cortex_m;

import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quartz.SchedulerException;

import java.time.Instant;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource
{
	@Inject
	TaskBean taskBean;

	@GET
	public List<ScheduledTask> listTasks()
	{
		return taskBean.listTasks();
	}

	@POST
	@Path("/oneshot")
	public ScheduledTask addOneShotTask(OneShotRequest req) throws SchedulerException
	{
		return taskBean.addTrigger(req.prompt(), req.executeAt());
	}

	@POST
	@Path("/cron")
	public ScheduledTask addCronTask(CronRequest req) throws SchedulerException
	{
		return taskBean.addCronTrigger(req.prompt(), new RecurringSchedule(req.cronExpression()), req.startAt());
	}

	@DELETE
	@Path("/{id}")
	public void deleteTask(@PathParam("id") Long id) throws SchedulerException
	{
		taskBean.deleteTask(id);
	}

	public record OneShotRequest(String prompt, Instant executeAt)
	{
	}

	public record CronRequest(String prompt, String cronExpression, Instant startAt)
	{
	}
}
