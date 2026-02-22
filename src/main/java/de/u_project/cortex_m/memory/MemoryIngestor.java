package de.u_project.cortex_m.memory;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

;

@ApplicationScoped
public class MemoryIngestor
{
	private static final int MAX_SEGMENT_SIZE_IN_CHARS = 500;
	private static final int MAX_OVERLAP_SIZE_IN_CHARS = 0;

	@Inject
	EmbeddingStore<TextSegment> store;

	@Inject
	EmbeddingModel embeddingModel;

	private RetrievalAugmentor augmenter;

	@PostConstruct
	void init()
	{
		EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
			.embeddingModel(embeddingModel)
			.embeddingStore(store)
			.maxResults(3)
			.build();
		augmenter = DefaultRetrievalAugmentor
			.builder()
			.contentRetriever(contentRetriever)
			.build();
	}

	public void ingest(String content)
	{
		Document doc = Document.document(content);
		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor
			.builder()
			.embeddingStore(store)
			.embeddingModel(embeddingModel)
			.documentSplitter(recursive(MAX_SEGMENT_SIZE_IN_CHARS, MAX_OVERLAP_SIZE_IN_CHARS))
			.build();
		ingestor.ingest(doc);
	}

	public String augment(String query)
	{
		UserMessage userMessage = UserMessage.userMessage(query);
		Metadata metadata = Metadata.from(userMessage, 1, List.of(userMessage));
		AugmentationRequest augmentationRequest = new AugmentationRequest(userMessage, metadata);
		// Improve return value. Does this include the full response?
		UserMessage result = (UserMessage)augmenter.augment(augmentationRequest).chatMessage();
		return result.toString();
	}
}
