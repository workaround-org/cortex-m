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
public class SoulTool implements CortexMTool
{
	private static final Logger log = LoggerFactory.getLogger(SoulTool.class);

	@Inject
	CortexMSoulRepository repository;

	@Tool("Persist the AI persona definition (the 'soul') to the database. " +
		"'soulText' should contain the full personality description, including name, traits, communication style, and any behavioral rules. " +
		"Call this only during onboarding when the soul does not yet exist. " +
		"Returns a confirmation with the assigned database id.")
	@Transactional
	public String saveSoul(String soulText)
	{
		CortexMSoul soul = new CortexMSoul();
		soul.setText(soulText);
		repository.persist(soul);
		log.info("Saved soul with id: {} and text {}", soul.getId(), soulText);
		return "Soul saved successfully with id: " + soul.getId();
	}
}
