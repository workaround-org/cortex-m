package de.u_project.cortex_m.scheduler;

import de.u_project.cortex_m.ConnectorWS;
import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class TaskBean
{
	private static final Logger log = LoggerFactory.getLogger(TaskBean.class);

	@Inject
	org.quartz.Scheduler quartz;

	@Inject
	CortexMBot bot;

	@Inject
	CortexMSoulRepository soulRepository;

	@Inject
	ConnectorWS connectorWS;

	public void addTrigger(String prompt, Instant executeAt) throws SchedulerException
	{
		JobDetail job = JobBuilder.newJob(MyJob.class)
			.withIdentity("myJob" + executeAt.toEpochMilli(), "myGroup")
			.withDescription(prompt)
			.build();
		Trigger trigger = TriggerBuilder.newTrigger()
			.withIdentity("myTrigger" + executeAt.toEpochMilli(), "myGroup")
			.startAt(Date.from(executeAt))
			.build();
		quartz.scheduleJob(job, trigger);
	}

	@Transactional
	void performTask(String prompt)
	{
		log.info("Executing task " + prompt);
		Optional<CortexMSoul> soul = soulRepository.findByIdOptional(1L);
		CortexMSoul fallbackSoul = new CortexMSoul();
		fallbackSoul.setText("You are a helpful assistant that performs tasks based on prompts. "
			+ "Always provide clear and concise responses to the given prompts, ensuring that you address the user's needs effectively.");

		String reply = bot.chat(prompt, "task", soul.orElseGet(() -> fallbackSoul).getText());
		// broadcast the reply to all connected clients
		connectorWS.broadCast(reply);
	}

	// A new instance of MyJob is created by Quartz for every job execution
	public static class MyJob implements Job
	{
		@Inject
		TaskBean taskBean;

		public void execute(JobExecutionContext context) throws JobExecutionException
		{
			String prompt = context.getJobDetail().getDescription();
			log.info("Prompt -> '{}'", prompt);
			taskBean.performTask(prompt);
		}
	}
}
