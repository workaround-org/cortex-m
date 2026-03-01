package de.u_project.cortex_m.database;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatMemoryRepository implements PanacheRepositoryBase<ChatMemory, String>
{
}
