package de.u_project.cortex_m;

import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.CortexMToolProvider;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestHTTPEndpoint(McpResource.class)
class McpResourceTest
{
	@Inject
	CortexMToolProvider mToolProvider;

	@Inject
	McpHttpConfigRepository mcpHttpConfigRepository;

	@BeforeEach
	@Transactional
	void cleanDb()
	{
		mcpHttpConfigRepository.deleteAll();
	}

	@Test
	void testGetMcpConnectionsEmpty()
	{
		given()
			.when()
			.get()
			.then()
			.statusCode(200)
			.body("$", hasSize(0));
	}

	@Test
	void testAddMcpConnection()
	{
		McpHttpConfig config = new McpHttpConfig();
		config.setName("new-mcp");
		config.setUrl("http://new-host/mcp/");

		given()
			.contentType(ContentType.JSON)
			.body(config)
			.when()
			.post()
			.then()
			.statusCode(200)
			.body("name", is("new-mcp"))
			.body("url", is("http://new-host/mcp/"));
	}

	@Test
	@Transactional
	void testDeleteMcpConnection()
	{
		McpHttpConfig config = new McpHttpConfig();
		config.setName("to-delete");
		config.setUrl("http://delete-me/mcp/");
		mcpHttpConfigRepository.persist(config);

		given()
			.when()
			.delete("/" + config.getId())
			.then()
			.statusCode(204);
	}
}

