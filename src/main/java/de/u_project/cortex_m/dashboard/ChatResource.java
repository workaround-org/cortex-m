package de.u_project.cortex_m.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import de.u_project.cortex_m.database.ChatMemory;
import de.u_project.cortex_m.database.ChatMemoryRepository;
import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Path("/dashboard/chats")
public class ChatResource extends DashboardSupport
{
    private static final String NO_TEXT = "[no text]";

    @Inject
    Template chat;

    @Inject
    Template chatDetail;

    @Inject
    ChatMemoryRepository repository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list()
    {
        return chat
            .data("chatSessions", toChatSessions(repository.listAll()))
            .data("base", uriInfo.getAbsolutePath().toString());
    }

    @GET
    @Path("{memoryId}")
    @Produces(MediaType.TEXT_HTML)
    public Response detail(@PathParam("memoryId") String encodedMemoryId)
    {
        String memoryId = decodePathSegment(encodedMemoryId);
        ChatMemory memory = repository.findById(memoryId);
        if (memory == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ChatSessionView session = toChatSession(memory);
        return Response.ok(chatDetail
            .data("session", session)
            .data("listBase", uriInfo.getBaseUriBuilder()
                .path("dashboard")
                .path("chats")
                .build()
                .toString()))
            .build();
    }

    @POST
    @Path("{memoryId}/delete")
    @Transactional
    public Response delete(@PathParam("memoryId") String encodedMemoryId)
    {
        String memoryId = decodePathSegment(encodedMemoryId);
        repository.deleteById(memoryId);
        return seeOther("chats");
    }

    private List<ChatSessionView> toChatSessions(List<ChatMemory> memories)
    {
        List<ChatSessionView> sessions = new ArrayList<>(memories.size());
        for (ChatMemory memory : memories)
        {
            sessions.add(toChatSession(memory));
        }
        return sessions;
    }

    private ChatSessionView toChatSession(ChatMemory memory)
    {
        String memoryId = memory.getMemoryId();
        return new ChatSessionView(memoryId, encodePathSegment(memoryId), parseMessages(memory.getMessagesJson()));
    }

    private List<ChatMessageView> parseMessages(String messagesJson)
    {
        try
        {
            JsonNode root = QuarkusJsonCodecFactory.ObjectMapperHolder.MAPPER.readTree(messagesJson);
            if (!root.isArray())
            {
                return List.of(new ChatMessageView("error", "Failed to parse chat memory: expected JSON array."));
            }

            List<ChatMessageView> messages = new ArrayList<>(root.size());
            for (JsonNode node : root)
            {
                messages.add(new ChatMessageView(resolveRole(node), resolveText(node)));
            }
            return messages;
        }
        catch (IOException e)
        {
            return List.of(new ChatMessageView("error", "Failed to parse chat memory: " + e.getMessage()));
        }
    }

    private String resolveRole(JsonNode node)
    {
        String type = firstNonBlank(
            text(node, "type"),
            text(node, "messageType"),
            text(node, "role"));
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);

        return switch (normalized)
        {
            case "SYSTEM" -> "system";
            case "USER" -> "user";
            case "AI", "ASSISTANT" -> "ai";
            case "TOOL_EXECUTION_RESULT", "TOOL" -> "tool";
            default -> hasToolResult(node) ? "tool" : "system";
        };
    }

    private boolean hasToolResult(JsonNode node)
    {
        return node.has("toolExecutionResult")
            || node.has("toolExecutionResults")
            || node.has("toolName")
            || node.has("toolExecutionRequest");
    }

    private String resolveText(JsonNode node)
    {
        String text = firstNonBlank(
            text(node, "text"),
            text(node, "content"),
            text(node.at("/toolExecutionResult/text")),
            text(node.at("/toolExecutionResult/result")),
            text(node.at("/toolExecutionResult/output")),
            text(node.at("/toolExecutionRequest/text")));
        if (text != null)
        {
            return text;
        }

        JsonNode contents = node.get("contents");
        if (contents != null && contents.isArray())
        {
            List<String> parts = new ArrayList<>(contents.size());
            for (JsonNode content : contents)
            {
                String part = firstNonBlank(
                    text(content, "text"),
                    text(content, "content"),
                    content.isTextual() ? content.asText() : null);
                if (part != null)
                {
                    parts.add(part);
                }
            }
            if (!parts.isEmpty())
            {
                return String.join("\n", parts);
            }
        }

        return NO_TEXT;
    }

    private String text(JsonNode node, String field)
    {
        if (node == null || field == null)
        {
            return null;
        }
        return text(node.get(field));
    }

    private String text(JsonNode node)
    {
        if (node == null || node.isMissingNode() || node.isNull())
        {
            return null;
        }
        if (node.isTextual())
        {
            return node.asText();
        }
        return null;
    }

    private String firstNonBlank(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.isBlank())
            {
                return value;
            }
        }
        return null;
    }

    private String encodePathSegment(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String decodePathSegment(String value)
    {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public record ChatSessionView(String memoryId, String encodedMemoryId, List<ChatMessageView> messages)
    {
    }

    public record ChatMessageView(String role, String text)
    {
    }
}
