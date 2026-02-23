package de.u_project.cortex_m.connector;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SessionManagerTest
{
	@Inject
	SessionManager sessionManager;

	@Test
	void testCreateSessionReturnsUniqueIds()
	{
		String id1 = sessionManager.createSession();
		String id2 = sessionManager.createSession();

		assertNotNull(id1);
		assertNotNull(id2);
		assertNotEquals(id1, id2);
	}

	@Test
	void testIsValidSessionAfterCreation()
	{
		String id = sessionManager.createSession();

		assertTrue(sessionManager.isValidSession(id));
	}

	@Test
	void testIsValidSessionReturnsFalseForUnknownId()
	{
		assertFalse(sessionManager.isValidSession("not-a-real-session"));
	}
}
