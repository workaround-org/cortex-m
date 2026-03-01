package de.u_project.cortex_m.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class ScheduledTask
{
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String jobName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String prompt;

	/** "ONE_SHOT" or "CRON" */
	@Column(nullable = false)
	private String taskType;

	/** Set for ONE_SHOT tasks */
	private Instant executeAt;

	/** Set for CRON tasks */
	private String cronExpression;

	/** Set for CRON tasks */
	private Instant startAt;

	public ScheduledTask()
	{
	}

	public Long getId()
	{
		return id;
	}

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public String getPrompt()
	{
		return prompt;
	}

	public void setPrompt(String prompt)
	{
		this.prompt = prompt;
	}

	public String getTaskType()
	{
		return taskType;
	}

	public void setTaskType(String taskType)
	{
		this.taskType = taskType;
	}

	public Instant getExecuteAt()
	{
		return executeAt;
	}

	public void setExecuteAt(Instant executeAt)
	{
		this.executeAt = executeAt;
	}

	public String getCronExpression()
	{
		return cronExpression;
	}

	public void setCronExpression(String cronExpression)
	{
		this.cronExpression = cronExpression;
	}

	public Instant getStartAt()
	{
		return startAt;
	}

	public void setStartAt(Instant startAt)
	{
		this.startAt = startAt;
	}

	@Override
	public String toString()
	{
		if ("CRON".equals(taskType))
		{
			return String.format("[id=%d] RECURRING | cron=%s | startAt=%s | prompt: %s",
				id, cronExpression, startAt, prompt);
		}
		else
		{
			return String.format("[id=%d] ONE-TIME  | executeAt=%s | prompt: %s",
				id, executeAt, prompt);
		}
	}
}
