package de.u_project.cortex_m.scheduler;

import de.u_project.cortex_m.ConnectorWS;
import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import de.u_project.cortex_m.database.ScheduledTask;
import de.u_project.cortex_m.database.ScheduledTaskRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TaskBean
{
	static final String TYPE_ONE_SHOT = "ONE_SHOT";
	static final String TYPE_CRON = "CRON";
	static final String JOB_DATA_TYPE = "taskType";
	static final String JOB_DATA_NAME = "jobName";
	private static final Logger log = LoggerFactory.getLogger(TaskBean.class);
	private static final String JOB_GROUP = "myGroup";

	@Inject
	org.quartz.Scheduler quartz;

	@Inject
	CortexMBot bot;

	@Inject
	CortexMSoulRepository soulRepository;

	@Inject
	ConnectorWS connectorWS;

	@Inject
	ScheduledTaskRepository scheduledTaskRepository;

	void onStart(@Observes StartupEvent event) throws SchedulerException
	{
		restorePersistedTasks();
	}

	@Transactional
	public ScheduledTask addTrigger(String prompt, Instant executeAt) throws SchedulerException
	{
		String jobName = "myJob" + executeAt.toEpochMilli();
		scheduleOneShotJob(jobName, prompt, executeAt);

		ScheduledTask task = new ScheduledTask();
		task.setJobName(jobName);
		task.setPrompt(prompt);
		task.setTaskType(TYPE_ONE_SHOT);
		task.setExecuteAt(executeAt);
		scheduledTaskRepository.persist(task);
		return task;
	}

	@Transactional
	public ScheduledTask addCronTrigger(String prompt, RecurringSchedule schedule, Instant startAt) throws SchedulerException
	{
		String jobName = "cronJob" + startAt.toEpochMilli();
		scheduleCronJob(jobName, prompt, schedule, startAt);

		ScheduledTask task = new ScheduledTask();
		task.setJobName(jobName);
		task.setPrompt(prompt);
		task.setTaskType(TYPE_CRON);
		task.setCronExpression(schedule.cronExpression());
		task.setStartAt(startAt);
		scheduledTaskRepository.persist(task);
		return task;
	}

	@Transactional
	public List<ScheduledTask> listTasks()
	{
		return scheduledTaskRepository.listAll();
	}

	@Transactional
	public void deleteTask(Long id) throws SchedulerException
	{
		ScheduledTask task = scheduledTaskRepository.findByIdOptional(id)
			.orElseThrow(() -> new IllegalArgumentException("No task found with id " + id));
		quartz.deleteJob(new org.quartz.JobKey(task.getJobName(), JOB_GROUP));
		scheduledTaskRepository.deleteById(id);
	}

	@Transactional
	void performTask(String prompt, String jobName, String taskType)
	{
		log.info("Executing task '{}'", prompt);
		String soulText = getSoulText();
		String reply = bot.executeTask(prompt, soulText, Instant.now().toString(), jobName + "_at_" + Instant.now().toString());
		connectorWS.broadCast(reply);

		// One-shot tasks are done after a single execution; remove from DB
		if (TYPE_ONE_SHOT.equals(taskType))
		{
			scheduledTaskRepository.deleteByJobName(jobName);
		}
	}

	private void restorePersistedTasks() throws SchedulerException
	{
		List<ScheduledTask> tasks = scheduledTaskRepository.listAll();
		quartz.clear();
		Instant now = Instant.now();
		for (ScheduledTask task : tasks)
		{
			if (TYPE_ONE_SHOT.equals(task.getTaskType()))
			{
				if (task.getExecuteAt().isAfter(now))
				{
					scheduleOneShotJob(task.getJobName(), task.getPrompt(), task.getExecuteAt());
				}
				else
				{
					// Execution window already passed — remove stale task
					log.warn("Removing stale one-shot task '{}' scheduled for {}", task.getJobName(), task.getExecuteAt());
					scheduledTaskRepository.deleteByJobName(task.getJobName());
				}
			}
			else
			{
				scheduleCronJob(task.getJobName(), task.getPrompt(),
					new RecurringSchedule(task.getCronExpression()), task.getStartAt());
			}
		}
		if (!tasks.isEmpty())
		{
			log.info("Restored {} persisted task(s) from database", tasks.size());
		}
	}

	private JobDetail buildJob(String jobName, String prompt, String taskType)
	{
		return JobBuilder.newJob(MyJob.class)
			.withIdentity(jobName, JOB_GROUP)
			.withDescription(prompt)
			.usingJobData(JOB_DATA_TYPE, taskType)
			.usingJobData(JOB_DATA_NAME, jobName)
			.build();
	}

	private void scheduleOneShotJob(String jobName, String prompt, Instant executeAt) throws SchedulerException
	{
		Trigger trigger = TriggerBuilder.newTrigger()
			.withIdentity("myTrigger" + executeAt.toEpochMilli(), JOB_GROUP)
			.startAt(Date.from(executeAt))
			.build();
		quartz.scheduleJob(buildJob(jobName, prompt, TYPE_ONE_SHOT), trigger);
	}

	private void scheduleCronJob(String jobName, String prompt, RecurringSchedule schedule, Instant startAt)
		throws SchedulerException
	{
		if (startAt.isBefore(Instant.now()))
		{
			startAt = Instant.now();
		}
		Trigger trigger = TriggerBuilder.newTrigger()
			.withIdentity("cronTrigger" + startAt.toEpochMilli(), JOB_GROUP)
			.startAt(Date.from(startAt))
			.withSchedule(CronScheduleBuilder.cronSchedule(schedule.cronExpression()))
			.build();
		quartz.scheduleJob(buildJob(jobName, prompt, TYPE_CRON), trigger);
	}

	private String getSoulText()
	{
		Optional<CortexMSoul> soul = soulRepository.findByIdOptional(1L);
		CortexMSoul fallbackSoul = new CortexMSoul();
		fallbackSoul.setText("You are a helpful assistant that performs tasks based on prompts. "
			+ "Always provide clear and concise responses to the given prompts, ensuring that you address the user's needs effectively.");
		return soul.orElseGet(() -> fallbackSoul).getText();
	}
}
