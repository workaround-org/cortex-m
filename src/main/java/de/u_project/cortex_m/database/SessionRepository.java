package de.u_project.cortex_m.database;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class SessionRepository implements PanacheRepositoryBase<Session, String>
{
	@Transactional
	public Session createSession()
	{
		Session session = new Session();
		session.setId(UUID.randomUUID().toString());
		session.setCreatedAt(Instant.now());
		persist(session);
		return session;
	}
}
