package de.u_project.cortex_m.connector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal CloudEvents 1.0 envelope.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CloudEvent(
	String specversion,
	String type,
	String source,
	String id,
	String time,
	String datacontenttype,
	Object data
)
{
}
