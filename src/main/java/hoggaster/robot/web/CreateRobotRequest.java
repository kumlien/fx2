package hoggaster.robot.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
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
    public final CurrencyPair currencyPair;

    @NotNull
    @Min(1)
    public final String depotId;

    @JsonCreator
    public CreateRobotRequest(
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "currencyPair") CurrencyPair currencyPair,
            @JsonProperty(value = "depotId") String depotId) {
        this.name = name;
        this.currencyPair = currencyPair;
        this.depotId = depotId;
    }
}
