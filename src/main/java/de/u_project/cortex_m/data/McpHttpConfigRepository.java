package de.u_project.cortex_m.data;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class McpHttpConfigRepository implements PanacheRepository<McpHttpConfig>
{

}
