package hoggaster.domain.robot.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;


public class ConditionRequest {

    @NotEmpty
    public final String name;

    @NotNull
    public final CurrencyPair currencyPair;

    @JsonCreator
    public ConditionRequest(
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "currencyPair") CurrencyPair currencyPair) {
        this.name = name;
        this.currencyPair = currencyPair;
    }
}
