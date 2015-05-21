package hoggaster.robot.web;

import javax.validation.constraints.NotNull;

import hoggaster.domain.Instrument;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ConditionRequest {
	
	@NotEmpty
	public final String name;
	
	@NotNull
	public final Instrument instrument;
	
	@JsonCreator
	public ConditionRequest(
			@JsonProperty(value="name") String name, 
			@JsonProperty(value="instrument") Instrument instrument) {
		this.name = name;
		this.instrument = instrument;
	}
}
