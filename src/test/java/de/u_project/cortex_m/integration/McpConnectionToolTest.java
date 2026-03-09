package de.u_project.cortex_m.integration;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import de.u_project.cortex_m.tools.buildin.McpConnectionTool;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@Tag("integration")
@QuarkusTest
public class McpConnectionToolTest
{
	private static final String SOUL = "You are professional and efficient assistant called Testy.";

	@Inject
	CortexMBot bot;
	@InjectSpy
	McpConnectionTool mcpTool;
	@Inject
	McpHttpConfigRepository mcpRepository;

	private static Stream<String> addMessages()
	{
		return Stream.of(
			"Add a new MCP server with the name 'TestServer' and the URL 'http://localhost:3000/mcp'",
			"Please add an MCP server named 'TestServer' with the URL 'http://localhost:3000/mcp'",
			"Can you set up an MCP connection called 'TestServer' pointing to 'http://localhost:3000/mcp'?",
			"I want to add an MCP server named 'TestServer' with the URL 'http://localhost:3000/mcp'",
			"Add an MCP server named 'TestServer' with the URL 'http://localhost:3000/mcp'"
		);
	}

	// ── addMcpConnection ─────────────────────────────────────────────────────

	private static Stream<String> addWithAuthMessages()
	{
		return Stream.of(
			"Add an MCP server named 'SecureServer' at 'http://localhost:3000/mcp' with auth header name 'X-API-Key' and value 'secret123'",
			"Register a new MCP connection called 'SecureServer' using URL 'http://localhost:3000/mcp', auth header 'X-API-Key', and token 'secret123'",
			"Set up MCP server 'SecureServer' at 'http://localhost:3000/mcp' with X-API-Key header set to secret123"
		);
	}

	private static Stream<String> listMessages()
	{
		return Stream.of(
			"List all configured MCP servers",
			"Show me all MCP connections",
			"What MCP servers do I have configured?"
		);
	}

	// ── addMcpConnection with auth ────────────────────────────────────────────

	private static Stream<String> updateMessages()
	{
		return Stream.of(
			"Update the MCP server 'TestServer' to use URL 'http://localhost:4000/mcp'",
			"Change the URL of MCP server 'TestServer' to 'http://localhost:4000/mcp'",
			"Update MCP connection 'TestServer': new URL is 'http://localhost:4000/mcp', no authentication"
		);
	}

	private static Stream<String> removeMessages()
	{
		return Stream.of(
			"Remove the MCP server named 'TestServer'",
			"Delete the MCP connection called 'TestServer'",
			"Remove MCP connection 'TestServer'"
		);
	}

	// ── listMcpConnections ────────────────────────────────────────────────────

	private String chat(String message)
	{
		String answer = bot.chat(message, UUID.randomUUID().toString(), SOUL, Instant.now().toString());
		System.out.println("Answer: " + answer);
		return answer;
	}

	@ParameterizedTest
	@MethodSource("addMessages")
	public void addNewMcpServer(String message)
	{
		QuarkusTransaction.begin();
		mcpRepository.deleteAll();
		QuarkusTransaction.commit();

		chat(message);
		verify(mcpTool).addMcpConnection("TestServer", "http://localhost:3000/mcp", null, null);
	}

	// ── updateMcpConnection ───────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("addWithAuthMessages")
	public void addMcpServerWithAuth(String message)
	{
		QuarkusTransaction.begin();
		mcpRepository.deleteAll();
		QuarkusTransaction.commit();

		chat(message);
		verify(mcpTool).addMcpConnection("SecureServer", "http://localhost:3000/mcp", "X-API-Key", "secret123");
	}

	@ParameterizedTest
	@MethodSource("listMessages")
	public void listMcpConnections(String message)
	{
		chat(message);
		verify(mcpTool).listMcpConnections();
	}

	// ── removeMcpConnection ───────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("updateMessages")
	public void updateMcpServer(String message)
	{
		QuarkusTransaction.begin();
		mcpRepository.deleteAll();
		McpHttpConfig config = new McpHttpConfig();
		config.setName("TestServer");
		config.setUrl("http://localhost:3000/mcp");
		mcpRepository.persist(config);
		QuarkusTransaction.commit();

		chat(message);
		verify(mcpTool).updateMcpConnection("TestServer", "http://localhost:4000/mcp", null, null);
	}

	@ParameterizedTest
	@MethodSource("removeMessages")
	public void removeMcpServer(String message)
	{
		QuarkusTransaction.begin();
		mcpRepository.deleteAll();
		McpHttpConfig config = new McpHttpConfig();
		config.setName("TestServer");
		config.setUrl("http://localhost:3000/mcp");
		mcpRepository.persist(config);
		QuarkusTransaction.commit();

		chat(message);
		verify(mcpTool).removeMcpConnection("TestServer");
	}
}
