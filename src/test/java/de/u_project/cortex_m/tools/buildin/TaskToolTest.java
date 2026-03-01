package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.ConnectorWS;
import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import de.u_project.cortex_m.scheduler.RecurringSchedule;
import de.u_project.cortex_m.scheduler.TaskBean;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TaskToolTest
{
	@InjectMock
	@MockitoConfig(convertScopes = true)
	CortexMBot bot;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	ConnectorWS connectorWS;

	@Inject
	TaskTool taskTool;

	@Inject
	TaskBean taskBean;

	@Inject
	ScheduledTaskRepository repository;

	// --- listTasks ---

	@Test
	@Transactional
	void listTasks_empty_returnsNoTasksMessage()
	{
		repository.deleteAll();

		assertEquals("No scheduled tasks.", taskTool.listTasks());
	}

	@Test
	@Transactional
	void listTasks_afterAddingTask_returnsFormattedEntry()
	{
		repository.deleteAll();
		taskTool.addTask(Instant.now().plusSeconds(120).toString(), "Say hello");

		String result = taskTool.listTasks();

		assertTrue(result.contains("ONE-TIME"));
		assertTrue(result.contains("Say hello"));
	}

	// --- addTask ---

	@Test
	@Transactional
	void addTask_success_returnsSuccessMessage()
	{
		repository.deleteAll();
		String executeAt = Instant.now().plusSeconds(120).toString();

		String result = taskTool.addTask(executeAt, "Ping");

		assertTrue(result.startsWith("Successfully added task"));
		assertTrue(result.contains(executeAt));
	}

	@Test
	void addTask_invalidTimestamp_returnsErrorMessage()
	{
		String result = taskTool.addTask("not-a-timestamp", "Ping");

		assertTrue(result.startsWith("Failed to add task"));
	}

	// --- addRecurringTask ---

	@Test
	@Transactional
	void addRecurringTask_success_returnsSuccessMessage()
	{
		repository.deleteAll();
		RecurringSchedule schedule = new RecurringSchedule("0 0 9 * * ?");

		String result = taskTool.addRecurringTask(schedule, Instant.now().toString(), "Daily report");

		assertTrue(result.startsWith("Successfully added recurring task"));
		assertTrue(result.contains("0 0 9 * * ?"));
	}

	// --- deleteTask ---

	@Test
	@Transactional
	void deleteTask_success_returnsSuccessMessage()
	{
		repository.deleteAll();
		taskTool.addTask(Instant.now().plusSeconds(120).toString(), "Temp");
		Long id = taskBean.listTasks().getFirst().getId();

		String result = taskTool.deleteTask(id);

		assertEquals("Successfully deleted task with id " + id, result);
	}

	@Test
	void deleteTask_notFound_returnsErrorMessage()
	{
		String result = taskTool.deleteTask(Long.MAX_VALUE);

		assertTrue(result.contains("No task found with id"));
	}
}
