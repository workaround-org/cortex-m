package de.u_project.cortex_m.connector;

import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Singleton
public class SessionManager
{
	// ToDo: Use Postgres later
	private final Set<String> activeSessions = new HashSet<>();

	public synchronized String createSession()
	{
		String sessionId = UUID.randomUUID().toString();
		activeSessions.add(sessionId);
		return sessionId;
	}

	public synchronized boolean isValidSession(String sessionId)
	{
		return activeSessions.contains(sessionId);
	}
}
