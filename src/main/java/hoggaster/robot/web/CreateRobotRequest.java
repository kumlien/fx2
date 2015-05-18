package hoggaster.robot.web;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import hoggaster.domain.Instrument;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class CreateRobotRequest {
	
	@NotEmpty
	public final String name;
	
	@NotEmpty
	public final Instrument instrument;
	
	@NotNull
	@Min(1)
	public final Long depotId;
	
	@JsonCreator
	public CreateRobotRequest(
			@JsonProperty(value="name") String name, 
			@JsonProperty(value="instrument") Instrument instrument, 
			@JsonProperty(value="depotId") Long depotId) {
		this.name = name;
		this.instrument = instrument;
		this.depotId = depotId;
	}
}
