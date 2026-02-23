package de.u_project.cortex_m;

import de.u_project.cortex_m.connector.SessionManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ConnectorResource.class)
class ConnectorResourceTest
{
	@InjectMock
	SessionManager sessionManager;

	@Test
	void testCreateSession()
	{
		when(sessionManager.createSession()).thenReturn("test-session-id");

		given()
			.when()
			.get()
			.then()
			.statusCode(200)
			.body(notNullValue());
	}
}
