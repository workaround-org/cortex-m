package de.u_project.cortex_m.connector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data payload for {@code assistant.message.inbound} CloudEvents.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InboundData(
	String connectorId,
	String conversationId,
	String roomId,
	String text
)
{
}
