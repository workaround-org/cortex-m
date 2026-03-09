package de.u_project.cortex_m.integration;

import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.tools.buildin.MemoryTool;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@Tag("integration")
@QuarkusTest
public class MemoryToolTest
{
	private static final String SOUL = "You are a professional and efficient assistant called Testy.";

	@Inject
	CortexMBot bot;
	@InjectSpy
	MemoryTool memoryTool;

	private static Stream<String> saveToMemoryMessages()
	{
		return Stream.of(
			"Remember that the user's favourite programming language is Java",
			"Please save that the user's favourite programming language is Java",
			"Store in memory: the user's favourite programming language is Java",
			"Keep a note that the user's favourite programming language is Java",
			"Make a note: the user's favourite programming language is Java"
		);
	}

	// ── saveToMemory ──────────────────────────────────────────────────────────

	private static Stream<String> queryMemoryMessages()
	{
		return Stream.of(
			"What do you know about the user's programming language preferences?",
			"Search your memory for the user's programming language preferences",
			"Do you remember anything about the user's favourite programming language?",
			"Look up in memory: user programming language preferences",
			"Recall what you know about the user's favourite programming language"
		);
	}

	private String chat(String message)
	{
		String answer = bot.chat(message, UUID.randomUUID().toString(), SOUL, Instant.now().toString());
		System.out.println("Answer: " + answer);
		return answer;
	}

	// ── queryMemory ───────────────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("saveToMemoryMessages")
	public void saveToMemory(String message)
	{
		chat(message);
		verify(memoryTool).saveToMemory(anyString());
	}

	@ParameterizedTest
	@MethodSource("queryMemoryMessages")
	public void queryMemory(String message)
	{
		chat(message);
		verify(memoryTool).queryMemory(anyString());
	}
}
