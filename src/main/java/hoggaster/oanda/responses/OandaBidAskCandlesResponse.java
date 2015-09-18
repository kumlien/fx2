package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"instrument", "granularity", "candles"})
public class OandaBidAskCandlesResponse {

    @JsonProperty("instrument")
    private String instrument;
    @JsonProperty("granularity")
    private String granularity;
    @JsonProperty("candles")
    @Valid
    private List<OandaBidAskCandle> candles = new ArrayList<OandaBidAskCandle>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The instrument
     */
    @JsonProperty("instrument")
    public String getInstrument() {
        return instrument;
    }

    /**
     * @param instrument The instrument
     */
    @JsonProperty("instrument")
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    /**
     * @return The granularity
     */
    @JsonProperty("granularity")
    public String getGranularity() {
        return granularity;
    }

    /**
     * @param granularity The granularity
     */
    @JsonProperty("granularity")
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    /**
     * @return The candles
     */
    @JsonProperty("candles")
    public List<OandaBidAskCandle> getCandles() {
        return candles;
    }

    /**
     * @param candles The candles
     */
    @JsonProperty("candles")
    public void setCandles(List<OandaBidAskCandle> candles) {
        this.candles = candles;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
