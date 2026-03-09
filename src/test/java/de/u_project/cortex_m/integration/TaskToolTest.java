package de.u_project.cortex_m.integration;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import de.u_project.cortex_m.tools.buildin.TaskTool;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.SchedulerException;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Tag("integration")
@QuarkusTest
public class TaskToolTest
{
	private static final String SOUL = "You are a professional and efficient assistant called Testy.";

	@Inject
	CortexMBot bot;
	@InjectSpy
	TaskTool taskTool;
	@Inject
	TaskBean taskBean;
	@Inject
	ScheduledTaskRepository taskRepository;

	private static Stream<String> addTaskMessages()
	{
		return Stream.of(
			"Schedule a task at 2030-01-01T09:00:00Z to send a morning greeting",
			"Please add a one-time task at 2030-01-01T09:00:00Z: send a morning greeting",
			"Run a task at 2030-01-01T09:00:00Z that sends a morning greeting",
			"At 2030-01-01T09:00:00Z, execute: send a morning greeting"
		);
	}

	private static Stream<String> addRecurringTaskMessages()
	{
		return Stream.of(
			"Schedule a recurring task every day at 9am starting 2030-06-01T00:00:00Z to check the news. Use cron '0 0 9 * * ?'",
			"Add a daily recurring task with cron '0 0 9 * * ?' starting at 2030-06-01T00:00:00Z: check the news",
			"Create a cron task '0 0 9 * * ?' starting 2030-06-01T00:00:00Z to check the news every day at 9am"
		);
	}

	// ── addTask ───────────────────────────────────────────────────────────────

	private static Stream<String> listTasksMessages()
	{
		return Stream.of(
			"List all my scheduled tasks",
			"Show me all scheduled tasks",
			"What tasks do I have scheduled?"
		);
	}

	private static Stream<String> getCurrentDateMessages()
	{
		return Stream.of(
			"What is the current date and time?",
			"What time is it right now in UTC?",
			"Give me the current timestamp"
		);
	}

	// ── addRecurringTask ──────────────────────────────────────────────────────

	private String chat(String message)
	{
		String answer = bot.chat(message, UUID.randomUUID().toString(), SOUL, Instant.now().toString());
		System.out.println("Answer: " + answer);
		return answer;
	}

	@BeforeEach
	void cleanUp()
	{
		QuarkusTransaction.begin();
		taskRepository.deleteAll();
		QuarkusTransaction.commit();
	}

	// ── listTasks ─────────────────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("addTaskMessages")
	public void addTask(String message)
	{
		chat(message);
		verify(taskTool).addTask(eq("2030-01-01T09:00:00Z"), anyString());
	}

	@ParameterizedTest
	@MethodSource("addRecurringTaskMessages")
	public void addRecurringTask(String message)
	{
		chat(message);
		verify(taskTool).addRecurringTask(eq(new RecurringSchedule("0 0 9 * * ?")), anyString(), anyString());
	}

	// ── deleteTask ────────────────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("listTasksMessages")
	public void listTasks(String message)
	{
		chat(message);
		verify(taskTool).listTasks();
	}

	@Test
	public void deleteTask() throws SchedulerException
	{
		ScheduledTask task = taskBean.addTrigger("Test task to be deleted", Instant.parse("2030-06-01T10:00:00Z"));
		Long taskId = task.getId();

		chat("Delete the scheduled task with id " + taskId);
		verify(taskTool).deleteTask(taskId);
	}

	// ── getCurrentDate ────────────────────────────────────────────────────────

	@Test
	public void deleteTask_withNaturalLanguage() throws SchedulerException
	{
		ScheduledTask task = taskBean.addTrigger("Test task to be deleted", Instant.parse("2030-06-01T10:00:00Z"));
		Long taskId = task.getId();

		chat("Remove the task with id " + taskId + " from the schedule");
		verify(taskTool).deleteTask(taskId);
	}

	@ParameterizedTest
	@MethodSource("getCurrentDateMessages")
	public void getCurrentDate(String message)
	{
		chat(message);
		verify(taskTool).getCurrentDate();
	}
}
