package de.u_project.cortex_m;

import de.u_project.cortex_m.connector.SessionManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("connector")
public class ConnectorResource
{
	@Inject
	SessionManager sessionManager;

	@GET
	public String create()
	{
		return sessionManager.createSession();
	}
}
