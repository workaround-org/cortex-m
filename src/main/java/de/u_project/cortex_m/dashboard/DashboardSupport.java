package de.u_project.cortex_m.dashboard;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

abstract class DashboardSupport
{
	@Context
	UriInfo uriInfo;

	protected Response seeOther(String section)
	{
		URI uri = uriInfo.getBaseUriBuilder()
			.path("dashboard")
			.path(section)
			.build();
		return Response.seeOther(uri).build();
	}

	protected static Instant parseInstant(String datetimeLocal, Instant fallback)
	{
		if (datetimeLocal == null || datetimeLocal.isBlank()) return fallback;
		return LocalDateTime.parse(datetimeLocal).toInstant(ZoneOffset.UTC);
	}
}
