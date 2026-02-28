package de.u_project.cortex_m.bot;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.quarkiverse.langchain4j.jaxrsclient.JaxRsHttpClientBuilderFactory;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

@ApplicationScoped
public class CortexMChatModelSupplier implements Supplier<ChatModel>
{
	private static final Logger log = LoggerFactory.getLogger(CortexMChatModelSupplier.class);

	private final SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
	
	private final String baseUrl = config.getValue("quarkus.langchain4j.ollama.base-url", String.class);
	// Custom property (RUN_TIME phase) — avoids BUILD_AND_RUN_TIME_FIXED model-name
	// which would be baked into the native binary and unoverridable at runtime.
	private final String modelName = config.getOptionalValue("cortex-m.ollama.model-name", String.class).orElse("qwen3:14b");
	private final double temperature = config.getOptionalValue("quarkus.langchain4j.ollama.chat-model.temperature", Double.class).orElse(1.0);
	private final Duration timeout = config.getOptionalValue("quarkus.langchain4j.timeout", Duration.class).orElse(Duration.ofSeconds(180));

	@Override
	public ChatModel get()
	{
		log.info("Building OllamaChatModel — baseUrl={}, model={}", baseUrl, modelName);

		return OllamaChatModel.builder()
			.baseUrl(baseUrl)
			.modelName(modelName)
			.temperature(temperature)
			.timeout(timeout)
			.httpClientBuilder(new JaxRsHttpClientBuilderFactory().create())
			.build();
	}
}
