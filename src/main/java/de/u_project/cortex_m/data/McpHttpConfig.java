package de.u_project.cortex_m.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
// extend something from Panache of IDs
public class McpHttpConfig
{
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String url;
	private String authHeaderName;
	private String authHeaderValue;

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

	public String getAuthHeaderName()
	{
		return authHeaderName;
	}

	public void setAuthHeaderName(String authHeaderName)
	{
		this.authHeaderName = authHeaderName;
	}

	public String getAuthHeaderValue()
	{
		return authHeaderValue;
	}

	public void setAuthHeaderValue(String authHeaderValue)
	{
		this.authHeaderValue = authHeaderValue;
	}
}
