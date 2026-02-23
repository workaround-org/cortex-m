package de.u_project.cortex_m.bot;

import io.quarkiverse.langchain4j.auth.ModelAuthProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class OllamaAuthProvider implements ModelAuthProvider
{
	@ConfigProperty(name = "ollama.api-key")
	Optional<String> apiKey;

	@Override
	public String getAuthorization(ModelAuthProvider.Input input)
	{
		return apiKey
			.filter(value -> !value.isBlank())
			.map(value -> "Bearer " + value)
			.orElse(null);
	}
}
