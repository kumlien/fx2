package hoggaster.oanda.responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "instrument", "granularity", "candles" })
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
     * 
     * @return The instrument
     */
    @JsonProperty("instrument")
    public String getInstrument() {
	return instrument;
    }

    /**
     * 
     * @param instrument
     *            The instrument
     */
    @JsonProperty("instrument")
    public void setInstrument(String instrument) {
	this.instrument = instrument;
    }

    /**
     * 
     * @return The granularity
     */
    @JsonProperty("granularity")
    public String getGranularity() {
	return granularity;
    }

    /**
     * 
     * @param granularity
     *            The granularity
     */
    @JsonProperty("granularity")
    public void setGranularity(String granularity) {
	this.granularity = granularity;
    }

    /**
     * 
     * @return The candles
     */
    @JsonProperty("candles")
    public List<OandaBidAskCandle> getCandles() {
	return candles;
    }

    /**
     * 
     * @param candles
     *            The candles
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
