package de.u_project.cortex_m.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecurringScheduleTest
{
	@Test
	void validCronExpression_accepted()
	{
		assertDoesNotThrow(() -> new RecurringSchedule("0 0 9 * * ?"));
	}

	@Test
	void everyThirtyMinutes_accepted()
	{
		assertDoesNotThrow(() -> new RecurringSchedule("0 0/30 * * * ?"));
	}

	@Test
	void invalidCronExpression_throwsIllegalArgumentException()
	{
		IllegalArgumentException ex = assertThrows(
			IllegalArgumentException.class,
			() -> new RecurringSchedule("not-a-cron")
		);
		assertTrue(ex.getMessage().contains("Invalid cron expression"));
	}

	@Test
	void cronExpression_accessorReturnsValue()
	{
		RecurringSchedule schedule = new RecurringSchedule("0 0 9 * * ?");
		assertEquals("0 0 9 * * ?", schedule.cronExpression());
	}
}
