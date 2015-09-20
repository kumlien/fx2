package hoggaster.robot.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.Instrument;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 */
public class CreateRobotRequest {

    @NotEmpty
    public final String name;

    @NotEmpty
    public final Instrument instrument;

    @NotNull
    @Min(1)
    public final String depotId;

    @JsonCreator
    public CreateRobotRequest(
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "instrument") Instrument instrument,
            @JsonProperty(value = "depotId") String depotId) {
        this.name = name;
        this.instrument = instrument;
        this.depotId = depotId;
    }
}
