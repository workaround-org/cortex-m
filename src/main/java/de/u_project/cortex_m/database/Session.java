package de.u_project.cortex_m.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Session
{
	@Id
	private String id;

	@Column(nullable = false)
	private Instant createdAt;

	public Session()
	{
	}

	public Session(String id, Instant createdAt)
	{
		this.id = id;
		this.createdAt = createdAt;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Instant getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt)
	{
		this.createdAt = createdAt;
	}
}
