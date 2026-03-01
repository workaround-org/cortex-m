package de.u_project.cortex_m.scheduler;

import de.u_project.cortex_m.ConnectorWS;
import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TaskBeanTest
{
	@InjectMock
	@MockitoConfig(convertScopes = true)
	CortexMBot bot;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	ConnectorWS connectorWS;

	@Inject
	TaskBean taskBean;

	@Inject
	ScheduledTaskRepository repository;

	// --- listTasks ---

	@Test
	@Transactional
	void listTasks_empty_returnsEmptyList()
	{
		List<ScheduledTask> result = taskBean.listTasks();
		assertTrue(result.isEmpty());
	}

	// --- addTrigger ---

	@Test
	@Transactional
	void addTrigger_persistsOneShotTask() throws Exception
	{
		Instant executeAt = Instant.now().plusSeconds(120);

		taskBean.addTrigger("Say hello", executeAt);

		List<ScheduledTask> tasks = taskBean.listTasks();
		assertEquals(1, tasks.size());
		ScheduledTask task = tasks.getFirst();
		assertEquals("ONE_SHOT", task.getTaskType());
		assertEquals("Say hello", task.getPrompt());
		assertEquals(executeAt, task.getExecuteAt());
	}

	// --- addCronTrigger ---

	@Test
	@Transactional
	void addCronTrigger_persistsCronTask() throws Exception
	{
		RecurringSchedule schedule = new RecurringSchedule("0 0 9 * * ?");
		Instant startAt = Instant.now();

		taskBean.addCronTrigger("Daily report", schedule, startAt);

		List<ScheduledTask> tasks = taskBean.listTasks();
		assertEquals(1, tasks.size());
		ScheduledTask task = tasks.getFirst();
		assertEquals("CRON", task.getTaskType());
		assertEquals("Daily report", task.getPrompt());
		assertEquals("0 0 9 * * ?", task.getCronExpression());
	}

	// --- deleteTask ---

	@Test
	@Transactional
	void deleteTask_removesTaskFromDatabaseAndScheduler() throws Exception
	{
		taskBean.addTrigger("Temp task", Instant.now().plusSeconds(120));

		Long id = taskBean.listTasks().getFirst().getId();
		taskBean.deleteTask(id);

		assertTrue(taskBean.listTasks().isEmpty());
	}

	@Test
	void deleteTask_notFound_throwsIllegalArgumentException()
	{
		assertThrows(IllegalArgumentException.class, () -> taskBean.deleteTask(Long.MAX_VALUE));
	}

	@BeforeEach
	@Transactional
	void clearDatabase()
	{
		repository.deleteAll();
	}
}
