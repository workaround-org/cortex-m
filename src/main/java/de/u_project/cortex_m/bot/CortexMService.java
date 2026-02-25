package de.u_project.cortex_m.bot;

import de.u_project.cortex_m.database.CortexMSoul;
import de.u_project.cortex_m.database.CortexMSoulRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class CortexMService
{
	@Inject
	CortexMBot cortexMBot;

	@Inject
	CortexMSoulRepository cortexMSoulRepository;

	@ActivateRequestContext
	public String chat(String message, Object memoryId)
	{
		Optional<CortexMSoul> soul = cortexMSoulRepository.findByIdOptional(1L);
		if (soul.isEmpty())
		{
			// Create a new soul if none exists
			return cortexMBot.onboard(message);
		}
		return cortexMBot.chat(message, memoryId, soul.get().getText());
	}
}
