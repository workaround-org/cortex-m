package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskTool implements CortexMTool
{
	private static final Logger log = LoggerFactory.getLogger(TaskTool.class);

	@Inject
	TaskBean taskBean;

	@Tool("Schedule a one-time task to execute at a specific moment. " +
		"'executeAt' must be an ISO-8601 UTC timestamp (e.g. '2024-12-01T09:00:00Z'). " +
		"'llmPrompt' describes what the agent should do when the time comes.")
	public String addTask(String executeAt, String llmPrompt)
	{
		try
		{
			// Improve prompt / llm call
			taskBean.addTrigger(llmPrompt, Instant.parse(executeAt));
			return "Successfully added task to execute at " + executeAt;
		}
		catch (SchedulerException | java.time.format.DateTimeParseException e)
		{
			log.error("Failed to add task to execute at {}: {}", executeAt, e.getMessage());
			return "Failed to add task to execute at " + executeAt + ": " + e.getMessage();
		}
	}

	@Tool("Schedule a recurring task using a Quartz cron expression (6 fields: seconds minutes hours day-of-month month day-of-week, " +
		"e.g. '0 0 9 * * ?' for daily at 9am, '0 0/30 * * * ?' for every 30 minutes). " +
		"'startDate' is an ISO-8601 UTC timestamp for when the schedule begins. " +
		"'llmPrompt' describes what the agent should do on each execution.")
	public String addRecurringTask(RecurringSchedule schedule, String startDate, String llmPrompt)
	{
		try
		{
			taskBean.addCronTrigger("Execute the following task: " + llmPrompt, schedule, Instant.parse(startDate));
			return "Successfully added recurring task with schedule: " + schedule.cronExpression();
		}
		catch (SchedulerException e)
		{
			log.error("Failed to add recurring task: {}", e.getMessage());
			return "Failed to add recurring task: " + e.getMessage();
		}
	}

	@Tool("List all scheduled tasks. Returns each task's id, type (ONE_SHOT or CRON), prompt, " +
		"and schedule details (executeAt for one-time tasks, cronExpression + startAt for recurring tasks). " +
		"Use the returned id to delete a specific task.")
	public String listTasks()
	{
		List<ScheduledTask> tasks = taskBean.listTasks();
		if (tasks.isEmpty())
		{
			return "No scheduled tasks.";
		}
		return tasks.stream()
			.map(ScheduledTask::toString)
			.collect(Collectors.joining("\n"));
	}

	@Tool("Delete a scheduled task by its id. Use listTasks first to find the id. " +
		"Works for both one-time and recurring tasks. The task is removed from the scheduler and will not execute again.")
	public String deleteTask(Long id)
	{
		try
		{
			taskBean.deleteTask(id);
			return "Successfully deleted task with id " + id;
		}
		catch (IllegalArgumentException e)
		{
			return e.getMessage();
		}
		catch (SchedulerException e)
		{
			log.error("Failed to delete task {}: {}", id, e.getMessage());
			return "Failed to delete task " + id + ": " + e.getMessage();
		}
	}

	@Tool("Get the current date and time in ISO-8601 UTC format. Useful for scheduling tasks or including timestamps in prompts.")
	public String getCurrentDate()
	{
		return Instant.now().toString();
	}
}
