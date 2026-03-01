package de.u_project.cortex_m.scheduler;

import jakarta.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A new instance of MyJob is created by Quartz for every job execution
public class MyJob implements Job
{
	private static final Logger log = LoggerFactory.getLogger(MyJob.class);
	
	@Inject
	TaskBean taskBean;

	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		String prompt = context.getJobDetail().getDescription();
		String jobName = context.getMergedJobDataMap().getString(TaskBean.JOB_DATA_NAME);
		String taskType = context.getMergedJobDataMap().getString(TaskBean.JOB_DATA_TYPE);
		log.info("Prompt -> '{}'", prompt);
		taskBean.performTask(prompt, jobName, taskType);
	}
}
