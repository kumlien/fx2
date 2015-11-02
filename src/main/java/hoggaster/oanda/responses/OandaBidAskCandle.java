package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"time", "openBid", "openAsk", "highBid", "highAsk",
        "lowBid", "lowAsk", "closeBid", "closeAsk", "volume", "complete"})
/**
 *
 * Representation of a bidask candle returned from Oanda. 
 *
 * @see BidAskCandle
 *
 */
public class OandaBidAskCandle {

    @JsonProperty("time")
    private String time;
    @JsonProperty("openBid")
    private BigDecimal openBid;
    @JsonProperty("openAsk")
    private BigDecimal openAsk;
    @JsonProperty("highBid")
    private BigDecimal highBid;
    @JsonProperty("highAsk")
    private BigDecimal highAsk;
    @JsonProperty("lowBid")
    private BigDecimal lowBid;
    @JsonProperty("lowAsk")
    private BigDecimal lowAsk;
    @JsonProperty("closeBid")
    private BigDecimal closeBid;
    @JsonProperty("closeAsk")
    private BigDecimal closeAsk;
    @JsonProperty("volume")
    private Integer volume;
    @JsonProperty("complete")
    private Boolean complete;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The time
     */
    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    /**
     * @param time The time
     */
    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * @return The openBid
     */
    @JsonProperty("openBid")
    public BigDecimal getOpenBid() {
        return openBid;
    }

    /**
     * @param openBid The openBid
     */
    @JsonProperty("openBid")
    public void setOpenBid(BigDecimal openBid) {
        this.openBid = openBid;
    }

    /**
     * @return The openAsk
     */
    @JsonProperty("openAsk")
    public BigDecimal getOpenAsk() {
        return openAsk;
    }

    /**
     * @param openAsk The openAsk
     */
    @JsonProperty("openAsk")
    public void setOpenAsk(BigDecimal openAsk) {
        this.openAsk = openAsk;
    }

    /**
     * @return The highBid
     */
    @JsonProperty("highBid")
    public BigDecimal getHighBid() {
        return highBid;
    }

    /**
     * @param highBid The highBid
     */
    @JsonProperty("highBid")
    public void setHighBid(BigDecimal highBid) {
        this.highBid = highBid;
    }

    /**
     * @return The highAsk
     */
    @JsonProperty("highAsk")
    public BigDecimal getHighAsk() {
        return highAsk;
    }

    /**
     * @param highAsk The highAsk
     */
    @JsonProperty("highAsk")
    public void setHighAsk(BigDecimal highAsk) {
        this.highAsk = highAsk;
    }

    /**
     * @return The lowBid
     */
    @JsonProperty("lowBid")
    public BigDecimal getLowBid() {
        return lowBid;
    }

    /**
     * @param lowBid The lowBid
     */
    @JsonProperty("lowBid")
    public void setLowBid(BigDecimal lowBid) {
        this.lowBid = lowBid;
    }

    /**
     * @return The lowAsk
     */
    @JsonProperty("lowAsk")
    public BigDecimal getLowAsk() {
        return lowAsk;
    }

    /**
     * @param lowAsk The lowAsk
     */
    @JsonProperty("lowAsk")
    public void setLowAsk(BigDecimal lowAsk) {
        this.lowAsk = lowAsk;
    }

    /**
     * @return The closeBid
     */
    @JsonProperty("closeBid")
    public BigDecimal getCloseBid() {
        return closeBid;
    }

    /**
     * @param closeBid The closeBid
     */
    @JsonProperty("closeBid")
    public void setCloseBid(BigDecimal closeBid) {
        this.closeBid = closeBid;
    }

    /**
     * @return The closeAsk
     */
    @JsonProperty("closeAsk")
    public BigDecimal getCloseAsk() {
        return closeAsk;
    }

    /**
     * @param closeAsk The closeAsk
     */
    @JsonProperty("closeAsk")
    public void setCloseAsk(BigDecimal closeAsk) {
        this.closeAsk = closeAsk;
    }

    /**
     * @return The volume
     */
    @JsonProperty("volume")
    public Integer getVolume() {
        return volume;
    }

    /**
     * @param volume The volume
     */
    @JsonProperty("volume")
    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    /**
     * @return The complete
     */
    @JsonProperty("complete")
    public Boolean getComplete() {
        return complete;
    }

    /**
     * @param complete The complete
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
