package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class TaskTool implements CortexMTool
{
	private static final Logger log = LoggerFactory.getLogger(TaskTool.class);

	@Inject
	TaskBean taskBean;

	void onStart(@Observes StartupEvent event) throws SchedulerException
	{
		if (LaunchMode.current().isDev())
		{
			// String reply = addTask(Instant.now().plusSeconds(20).toString(), "Send a message that its party time!");
			// String reply = addRecurringTask(new RecurringSchedule("0/30 * * * * ?"), Instant.now().toString(), "Send a message that its party time!");
			// log.info(reply);
		}
	}

	@Tool("Schedule a one-time task for Cortex-M to execute at a specific moment. " +
		"'executeAt' must be an ISO-8601 UTC timestamp (e.g. '2024-12-01T09:00:00Z'). " +
		"'llmPrompt' describes what Cortex-M should do when the time comes.")
	public String addTask(String executeAt, String llmPrompt)
	{
		try
		{
			// Improve prompt / llm call
			taskBean.addTrigger("Execute the following task: " + llmPrompt, Instant.parse(executeAt));
			return "Successfully added task to execute at " + executeAt;
		}
		catch (SchedulerException e)
		{
			log.error("Failed to add task to execute at {}: {}", executeAt, e.getMessage());
			return "Failed to add task to execute at " + executeAt + ": " + e.getMessage();
		}
	}

	@Tool("Schedule a recurring task for Cortex-M using a Quartz cron expression (6 fields: seconds minutes hours day-of-month month day-of-week, " +
		"e.g. '0 0 9 * * ?' for daily at 9am, '0 0/30 * * * ?' for every 30 minutes). " +
		"'startDate' is an ISO-8601 UTC timestamp for when the schedule begins. " +
		"'llmPrompt' describes what Cortex-M should do on each execution.")
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
}
