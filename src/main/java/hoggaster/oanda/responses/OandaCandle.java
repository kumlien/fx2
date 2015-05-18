package hoggaster.oanda.responses;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OandaCandle {
	
	public final Date time;
	public final Double openMid;
	public final Double highMid;
	public final Double closeMid;
	public final Long volume;
	public final Boolean complete;
	
	@JsonCreator
	public OandaCandle(
			@JsonProperty(value="time") Date time, 
			@JsonProperty(value="openMid") Double openMid, 
			@JsonProperty(value="highMid") Double highMid, 
			@JsonProperty(value="closeMid") Double closeMid,
			@JsonProperty(value="volume")Long volume, 
			@JsonProperty(value="complete")Boolean complete) {
		this.time = time;
		this.openMid = openMid;
		this.highMid = highMid;
		this.closeMid = closeMid;
		this.volume = volume;
		this.complete = complete;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Candle [time=").append(time).append(", openMid=")
				.append(openMid).append(", highMid=").append(highMid)
				.append(", closeMid=").append(closeMid).append(", volume=")
				.append(volume).append(", complete=").append(complete)
				.append("]");
		return builder.toString();
	}
}
