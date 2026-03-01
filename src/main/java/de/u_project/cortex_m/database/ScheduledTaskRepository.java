package de.u_project.cortex_m.database;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ScheduledTaskRepository implements PanacheRepositoryBase<ScheduledTask, Long>
{
	@Transactional
	public void deleteByJobName(String jobName)
	{
		delete("jobName", jobName);
	}
}
