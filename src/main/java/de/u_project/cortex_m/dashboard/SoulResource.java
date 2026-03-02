package de.u_project.cortex_m.dashboard;

import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
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

@Path("/dashboard/souls")
public class SoulResource extends DashboardSupport
{
	@Inject
	Template soul;

	@Inject
	CortexMSoulRepository repository;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance list()
	{
		return soul
			.data("souls", repository.listAll())
			.data("base", uriInfo.getAbsolutePath().toString());
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response create(@FormParam("text") String text)
	{
		CortexMSoul s = new CortexMSoul();
		s.setText(text);
		repository.persist(s);
		return seeOther("souls");
	}

	@POST
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response update(@PathParam("id") Long id, @FormParam("text") String text)
	{
		CortexMSoul s = repository.findById(id);
		if (s == null) return Response.status(Response.Status.NOT_FOUND).build();
		s.setText(text);
		return seeOther("souls");
	}

	@POST
	@Path("{id}/delete")
	@Transactional
	public Response delete(@PathParam("id") Long id)
	{
		repository.deleteById(id);
		return seeOther("souls");
	}
}
