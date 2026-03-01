package de.u_project.cortex_m.database;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskTest
{
	private ScheduledTask buildTask(String type)
	{
		ScheduledTask task = new ScheduledTask();
		task.setTaskType(type);
		task.setPrompt("Do something useful");
		return task;
	}

	@Test
	void toString_oneShotTask_containsExpectedFields()
	{
		ScheduledTask task = buildTask("ONE_SHOT");
		Instant at = Instant.parse("2025-06-01T09:00:00Z");
		task.setExecuteAt(at);

		String result = task.toString();

		assertTrue(result.contains("ONE-TIME"), "should label as ONE-TIME");
		assertTrue(result.contains(at.toString()), "should include executeAt");
		assertTrue(result.contains("Do something useful"), "should include prompt");
		assertFalse(result.contains("RECURRING"), "should not say RECURRING");
	}

	@Test
	void toString_cronTask_containsExpectedFields()
	{
		ScheduledTask task = buildTask("CRON");
		Instant start = Instant.parse("2025-01-01T00:00:00Z");
		task.setCronExpression("0 0 9 * * ?");
		task.setStartAt(start);

		String result = task.toString();

		assertTrue(result.contains("RECURRING"), "should label as RECURRING");
		assertTrue(result.contains("0 0 9 * * ?"), "should include cron expression");
		assertTrue(result.contains(start.toString()), "should include startAt");
		assertTrue(result.contains("Do something useful"), "should include prompt");
		assertFalse(result.contains("ONE-TIME"), "should not say ONE-TIME");
	}
}
