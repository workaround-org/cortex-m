package de.u_project.cortex_m.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ChatMemory
{
	@Id
	private String memoryId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String messagesJson;

	public ChatMemory()
	{
	}

	public ChatMemory(String memoryId, String messagesJson)
	{
		this.memoryId = memoryId;
		this.messagesJson = messagesJson;
	}

	public String getMemoryId()
	{
		return memoryId;
	}

	public void setMemoryId(String memoryId)
	{
		this.memoryId = memoryId;
	}

	public String getMessagesJson()
	{
		return messagesJson;
	}

	public void setMessagesJson(String messagesJson)
	{
		this.messagesJson = messagesJson;
	}
}
