package de.u_project.cortex_m.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class McpHttpConfig
{
	@Id
	private Long id;
	private String name;
	private String url;
	private String authHeader;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getAuthHeader()
	{
		return authHeader;
	}

	public void setAuthHeader(String authHeader)
	{
		this.authHeader = authHeader;
	}
}
