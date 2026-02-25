package de.u_project.cortex_m;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.memory.MemoryIngestor;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ChatResource.class)
class ChatResourceTest
{
	@InjectMock
	CortexMBot cortexMBot;

	@InjectMock
	MemoryIngestor memoryIngestor;

	@Test
	void testChat()
	{
		when(cortexMBot.chat("Hello", "test-memory", null)).thenReturn("Hi there!");

		given()
			.contentType("text/plain")
			.body("Hello")
			.when()
			.post()
			.then()
			.statusCode(200)
			.body(is("Hi there!"));
	}

	@Test
	void testIngestMemory()
	{
		doNothing().when(memoryIngestor).ingest(anyString());

		given()
			.contentType("text/plain")
			.body("some data")
			.when()
			.post("memory")
			.then()
			.statusCode(204);
	}

	@Test
	void testAugmentMemory()
	{
		when(memoryIngestor.augment("query")).thenReturn("augmented result");

		given()
			.contentType("text/plain")
			.body("query")
			.when()
			.post("memory/augment")
			.then()
			.statusCode(200)
			.body(is("augmented result"));
	}
}
