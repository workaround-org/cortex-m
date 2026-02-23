package de.u_project.cortex_m;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.u_project.cortex_m.bot.CortexMBot;
import de.u_project.cortex_m.connector.CloudEvent;
import de.u_project.cortex_m.connector.InboundData;
import de.u_project.cortex_m.connector.OutboundData;
import de.u_project.cortex_m.connector.SessionManager;
import io.quarkus.logging.Log;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.websockets.next.*;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@WebSocket(path = "/connector/{session}")
public class ConnectorWS
{
	static final String EVENT_TYPE_INBOUND = "assistant.message.inbound";
	static final String EVENT_TYPE_OUTBOUND = "assistant.message.outbound";
	static final String CORTEX_SOURCE = "urn:cortex-m";

	@Inject
	CortexMBot cortexMBot;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	WebSocketConnection connection;

	@Inject
	SessionManager sessionManager;

	@OnOpen
	public void onOpen() throws InterruptedException
	{
		String session = connection.pathParam("session");
		Log.infof("Connector connected: session=%s", session);
		if (!sessionManager.isValidSession(session))
		{
			Log.warnf("Invalid session ID: %s. Closing connection.", session);
			connection.close().wait();
			throw new UnauthorizedException();
		}
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
		String reply = cortexMBot.chat(inbound.text());

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
}
