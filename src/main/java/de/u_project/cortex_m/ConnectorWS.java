package de.u_project.cortex_m;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.u_project.cortex_m.bot.CortexMService;
import de.u_project.cortex_m.connector.CloudEvent;
import de.u_project.cortex_m.connector.InboundData;
import de.u_project.cortex_m.connector.OutboundData;
import de.u_project.cortex_m.connector.SessionManager;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@WebSocket(path = "/connector/{session}")
public class ConnectorWS
{
	static final String EVENT_TYPE_INBOUND = "assistant.message.inbound";
	static final String EVENT_TYPE_OUTBOUND = "assistant.message.outbound";
	static final String CORTEX_SOURCE = "urn:cortex-m";
	private static final Logger log = LoggerFactory.getLogger(ConnectorWS.class);

	@Inject
	CortexMService cortexMBot;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	WebSocketConnection connection;

	@Inject
	SessionManager sessionManager;

	@Inject
	OpenConnections openConnections;

	@OnOpen
	@Blocking
	@ActivateRequestContext
	public Uni<Void> onOpen()
	{
		String session = connection.pathParam("session");
		Log.infof("Connector connected: session=%s", session);
		if (!sessionManager.isValidSession(session))
		{
			Log.warnf("Invalid session ID: %s. Closing connection.", session);
			return connection.close();
		}
		return Uni.createFrom().voidItem();
	}

	@OnClose
	public void onClose()
	{
		Log.infof("Connector disconnected: session=%s", connection.pathParam("session"));
	}

	@OnTextMessage
	@Blocking
	public CloudEvent onMessage(CloudEvent envelope) throws Exception
	{
		if (!EVENT_TYPE_INBOUND.equals(envelope.type()))
		{
			Log.warnf("Ignoring unknown CloudEvent type: %s", envelope.type());
			return null;
		}

		InboundData inbound = objectMapper.convertValue(envelope.data(), InboundData.class);
		String reply;
		log.debug("Get message for conversion: {}", inbound.conversationId());
		try
		{
			reply = cortexMBot.chat(inbound.text(), connection.pathParam("session"));
		}
		catch (Exception ex)
		{
			log.error("Failed to execute Ai request", ex);
			reply = "Sorry, something went wrong while processing your message ❌ " + ex.getClass().getSimpleName();
		}
		log.debug("Reply generated for connector: {}", inbound.connectorId());

		OutboundData outboundData = new OutboundData(
			inbound.connectorId(),
			inbound.conversationId(),
			reply
		);

		return new CloudEvent(
			"1.0",
			EVENT_TYPE_OUTBOUND,
			CORTEX_SOURCE,
			UUID.randomUUID().toString(),
			Instant.now().toString(),
			"application/json",
			outboundData
		);
	}

	public void broadCast(String message)
	{
		OutboundData outboundData = new OutboundData(
			null,
			"broadcast",
			message
		);

		CloudEvent cloudEvent = new CloudEvent(
			"1.0",
			EVENT_TYPE_OUTBOUND,
			CORTEX_SOURCE,
			UUID.randomUUID().toString(),
			Instant.now().toString(),
			"application/json",
			outboundData
		);

		String json;
		try
		{
			json = objectMapper.writeValueAsString(cloudEvent);
		}
		catch (Exception e)
		{
			log.error("Failed to serialize CloudEvent", e);
			return;
		}

		for (WebSocketConnection conn : openConnections.listAll())
		{
			conn.sendText(json).await().atMost(Duration.ofSeconds(60));
		}
	}
}
