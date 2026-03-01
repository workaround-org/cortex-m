package de.u_project.cortex_m.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.u_project.cortex_m.database.ChatMemory;
import de.u_project.cortex_m.database.ChatMemoryRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgresChatMemoryStore implements ChatMemoryStore
{
	private static final TypeReference<List<ChatMessage>> MESSAGE_LIST_TYPE = new TypeReference<>() {
	};

	@Inject
	ChatMemoryRepository repository;

	@Override
	@Transactional
	public List<ChatMessage> getMessages(Object memoryId)
	{
		Optional<ChatMemory> stored = repository.findByIdOptional(memoryId.toString());
		if (stored.isEmpty())
		{
			return Collections.emptyList();
		}
		try
		{
			return QuarkusJsonCodecFactory.ObjectMapperHolder.MAPPER.readValue(
					stored.get().getMessagesJson(), MESSAGE_LIST_TYPE);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	@Transactional
	public void updateMessages(Object memoryId, List<ChatMessage> messages)
	{
		try
		{
			String json = QuarkusJsonCodecFactory.ObjectMapperHolder.MAPPER.writeValueAsString(messages);
			ChatMemory chatMemory = repository.findByIdOptional(memoryId.toString())
					.orElseGet(() -> new ChatMemory(memoryId.toString(), json));
			chatMemory.setMessagesJson(json);
			repository.persist(chatMemory);
		}
		catch (JsonProcessingException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	@Transactional
	public void deleteMessages(Object memoryId)
	{
		repository.deleteById(memoryId.toString());
	}
}
