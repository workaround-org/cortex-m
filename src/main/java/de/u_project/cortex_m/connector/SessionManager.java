package de.u_project.cortex_m.connector;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class SessionManager
{
	// ToDo: Use Postgres later
	private static final Set<String> ACTIVE_SESSIONS = new HashSet<>();

	public synchronized String createSession()
	{
		String sessionId = UUID.randomUUID().toString();
		ACTIVE_SESSIONS.add(sessionId);
		return sessionId;
	}

	public synchronized boolean isValidSession(String sessionId)
	{
		return ACTIVE_SESSIONS.contains(sessionId);
	}
}
