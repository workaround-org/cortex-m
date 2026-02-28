package de.u_project.cortex_m.tools.buildin;

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
			String reply = addTask(Instant.now().plusSeconds(20).toString(), "Send a message that its party time!");
			log.info(reply);
		}
	}

	@Tool("Adds a task to be executed at a specific time by Cortex-M")
	public String addTask(String executeAt, String llmPrompt)
	{
		try
		{
			// Improve prompt / llm call
			taskBean.addTrigger("Run the following task now: " + llmPrompt, Instant.parse(executeAt));
			return "Successfully added task to execute at " + executeAt;
		}
		catch (SchedulerException e)
		{
			log.error("Failed to add task to execute at {}: {}", executeAt, e.getMessage());
			return "Failed to add task to execute at " + executeAt + ": " + e.getMessage();
		}
	}
}
