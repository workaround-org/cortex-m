package de.u_project.cortex_m.connector;

/**
 * Data payload for {@code assistant.message.outbound} CloudEvents.
 */
public record OutboundData(
	String connectorId,
	String conversationId,
	String text
)
{
}
