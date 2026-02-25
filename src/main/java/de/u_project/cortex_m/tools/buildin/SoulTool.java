package de.u_project.cortex_m.tools.buildin;

import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SoulTool
{
	private static final Logger log = LoggerFactory.getLogger(SoulTool.class);
	
	@Inject
	CortexMSoulRepository repository;

	@Tool
	@Transactional
	public void saveSoul(String soulText)
	{
		// Save the soul data to the database
		CortexMSoul soul = new CortexMSoul();
		soul.setText(soulText);
		repository.persist(soul);
		log.info("Saved soul with id: {} and text {}", soul.getId(), soulText);
	}
}
