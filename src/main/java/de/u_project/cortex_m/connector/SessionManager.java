package de.u_project.cortex_m.connector;

import de.u_project.cortex_m.database.Session;
import de.u_project.cortex_m.database.SessionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SessionManager
{
	@Inject
	SessionRepository sessionRepository;

	@Transactional
	public String createSession()
	{
		Session session = sessionRepository.createSession();
		return session.getId();
	}

	public boolean isValidSession(String sessionId)
	{
		return sessionRepository.findByIdOptional(sessionId).isPresent();
	}
}
