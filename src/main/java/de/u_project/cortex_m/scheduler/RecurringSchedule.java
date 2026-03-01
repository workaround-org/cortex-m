package de.u_project.cortex_m.scheduler;

import org.quartz.CronExpression;

import java.text.ParseException;

/**
 * Wraps a Quartz cron expression and validates it on construction.
 * Example: "0 0 9 * * ?" → every day at 9am
 */
public record RecurringSchedule(String cronExpression)
{
	public RecurringSchedule
	{
		try
		{
			CronExpression.validateExpression(cronExpression);
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException("Invalid cron expression: " + cronExpression, e);
		}
	}
}
