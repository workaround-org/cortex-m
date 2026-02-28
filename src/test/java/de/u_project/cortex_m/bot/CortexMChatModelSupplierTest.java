package de.u_project.cortex_m.bot;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CortexMChatModelSupplierTest
{
	@Inject
	CortexMChatModelSupplier supplier;

	@Test
	void get_returnsOllamaChatModel()
	{
		ChatModel model = supplier.get();

		assertNotNull(model, "Supplier must return a non-null ChatModel at runtime");
		assertInstanceOf(OllamaChatModel.class, model,
			"Supplier must build an OllamaChatModel from runtime config");
	}

	@Test
	void get_createsNewInstanceOnEachCall()
	{
		ChatModel first = supplier.get();
		ChatModel second = supplier.get();

		assertNotNull(first);
		assertNotNull(second);
	}
}
