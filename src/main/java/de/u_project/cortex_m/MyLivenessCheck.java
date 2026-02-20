package de.u_project.cortex_m;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
public class MyLivenessCheck implements HealthCheck
{

	@Override
	public HealthCheckResponse call()
	{
		return HealthCheckResponse.up("alive");
	}

}