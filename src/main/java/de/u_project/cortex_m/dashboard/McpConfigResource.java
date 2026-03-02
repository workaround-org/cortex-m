package de.u_project.cortex_m.dashboard;

import de.u_project.cortex_m.database.McpHttpConfig;
import de.u_project.cortex_m.database.McpHttpConfigRepository;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard/mcp-configs")
public class McpConfigResource extends DashboardSupport
{
	@Inject
	Template dashboard;

	@Inject
	McpHttpConfigRepository repository;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance list()
	{
		return dashboard
			.data("configs", repository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response create(
		@FormParam("name") String name,
		@FormParam("url") String url,
		@FormParam("authHeaderName") String authHeaderName,
		@FormParam("authHeaderValue") String authHeaderValue)
	{
		McpHttpConfig config = new McpHttpConfig();
		config.setName(name);
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		repository.persist(config);
		return seeOther("mcp-configs");
	}

	@POST
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response update(
		@PathParam("id") Long id,
		@FormParam("name") String name,
		@FormParam("url") String url,
		@FormParam("authHeaderName") String authHeaderName,
		@FormParam("authHeaderValue") String authHeaderValue)
	{
		McpHttpConfig config = repository.findById(id);
		if (config == null) return Response.status(Response.Status.NOT_FOUND).build();
		config.setName(name);
		config.setUrl(url);
		config.setAuthHeaderName(authHeaderName);
		config.setAuthHeaderValue(authHeaderValue);
		return seeOther("mcp-configs");
	}

	@POST
	@Path("{id}/delete")
	@Transactional
	public Response delete(@PathParam("id") Long id)
	{
		repository.deleteById(id);
		return seeOther("mcp-configs");
	}
}
