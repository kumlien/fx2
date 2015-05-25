package hoggaster.oanda.responses;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "time", "openBid", "openAsk", "highBid", "highAsk",
		"lowBid", "lowAsk", "closeBid", "closeAsk", "volume", "complete" })
public class OandaBidAskCandle {

	@JsonProperty("time")
	private String time;
	@JsonProperty("openBid")
	private Double openBid;
	@JsonProperty("openAsk")
	private Double openAsk;
	@JsonProperty("highBid")
	private Double highBid;
	@JsonProperty("highAsk")
	private Double highAsk;
	@JsonProperty("lowBid")
	private Double lowBid;
	@JsonProperty("lowAsk")
	private Double lowAsk;
	@JsonProperty("closeBid")
	private Double closeBid;
	@JsonProperty("closeAsk")
	private Double closeAsk;
	@JsonProperty("volume")
	private Integer volume;
	@JsonProperty("complete")
	private Boolean complete;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * @return The time
	 */
	@JsonProperty("time")
	public String getTime() {
		return time;
	}

	/**
	 * 
	 * @param time
	 *            The time
	 */
	@JsonProperty("time")
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * 
	 * @return The openBid
	 */
	@JsonProperty("openBid")
	public Double getOpenBid() {
		return openBid;
	}

	/**
	 * 
	 * @param openBid
	 *            The openBid
	 */
	@JsonProperty("openBid")
	public void setOpenBid(Double openBid) {
		this.openBid = openBid;
	}

	/**
	 * 
	 * @return The openAsk
	 */
	@JsonProperty("openAsk")
	public Double getOpenAsk() {
		return openAsk;
	}

	/**
	 * 
	 * @param openAsk
	 *            The openAsk
	 */
	@JsonProperty("openAsk")
	public void setOpenAsk(Double openAsk) {
		this.openAsk = openAsk;
	}

	/**
	 * 
	 * @return The highBid
	 */
	@JsonProperty("highBid")
	public Double getHighBid() {
		return highBid;
	}

	/**
	 * 
	 * @param highBid
	 *            The highBid
	 */
	@JsonProperty("highBid")
	public void setHighBid(Double highBid) {
		this.highBid = highBid;
	}

	/**
	 * 
	 * @return The highAsk
	 */
	@JsonProperty("highAsk")
	public Double getHighAsk() {
		return highAsk;
	}

	/**
	 * 
	 * @param highAsk
	 *            The highAsk
	 */
	@JsonProperty("highAsk")
	public void setHighAsk(Double highAsk) {
		this.highAsk = highAsk;
	}

	/**
	 * 
	 * @return The lowBid
	 */
	@JsonProperty("lowBid")
	public Double getLowBid() {
		return lowBid;
	}

	/**
	 * 
	 * @param lowBid
	 *            The lowBid
	 */
	@JsonProperty("lowBid")
	public void setLowBid(Double lowBid) {
		this.lowBid = lowBid;
	}

	/**
	 * 
	 * @return The lowAsk
	 */
	@JsonProperty("lowAsk")
	public Double getLowAsk() {
		return lowAsk;
	}

	/**
	 * 
	 * @param lowAsk
	 *            The lowAsk
	 */
	@JsonProperty("lowAsk")
	public void setLowAsk(Double lowAsk) {
		this.lowAsk = lowAsk;
	}

	/**
	 * 
	 * @return The closeBid
	 */
	@JsonProperty("closeBid")
	public Double getCloseBid() {
		return closeBid;
	}

	/**
	 * 
	 * @param closeBid
	 *            The closeBid
	 */
	@JsonProperty("closeBid")
	public void setCloseBid(Double closeBid) {
		this.closeBid = closeBid;
	}

	/**
	 * 
	 * @return The closeAsk
	 */
	@JsonProperty("closeAsk")
	public Double getCloseAsk() {
		return closeAsk;
	}

	/**
	 * 
	 * @param closeAsk
	 *            The closeAsk
	 */
	@JsonProperty("closeAsk")
	public void setCloseAsk(Double closeAsk) {
		this.closeAsk = closeAsk;
	}

	/**
	 * 
	 * @return The volume
	 */
	@JsonProperty("volume")
	public Integer getVolume() {
		return volume;
	}

	/**
	 * 
	 * @param volume
	 *            The volume
	 */
	@JsonProperty("volume")
	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	/**
	 * 
	 * @return The complete
	 */
	@JsonProperty("complete")
	public Boolean getComplete() {
		return complete;
	}

	/**
	 * 
	 * @param complete
	 *            The complete
	 */
	@JsonProperty("complete")
	public void setComplete(Boolean complete) {
		this.complete = complete;
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
