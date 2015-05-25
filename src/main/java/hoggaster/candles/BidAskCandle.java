package hoggaster.candles;

import hoggaster.domain.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({
	@CompoundIndex(name="instrument_granularity_time_idx", def="{'instrument':1, 'granularity':1, 'time': 1}", unique=true)
})
public class BidAskCandle {
	
	@Id
	private String id;
	
	public final Instrument instrument;
	public final BrokerID brokerId;
	public final CandleStickGranularity granularity;
	
	public final Instant time;
	public final Double openBid;
	public final Double openAsk;
	public final Double highBid;
	public final Double highAsk;
	public final Double lowBid;
	public final Double lowAsk;
	public final Double closeBid;
	public final Double closeAsk;
	public final Integer volume;
	public final Boolean complete;
	
	@PersistenceConstructor
	BidAskCandle(String id, Instrument instrument, BrokerID brokerId,
			CandleStickGranularity granularity, Instant time, Double openBid,
			Double openAsk, Double highBid, Double highAsk, Double lowBid,
			Double lowAsk, Double closeBid, Double closeAsk, Integer volume,
			Boolean complete) {
		this.id=id;
		this.instrument = instrument;
		this.brokerId = brokerId;
		this.granularity = granularity;
		this.time = time;
		this.openBid = openBid;
		this.openAsk = openAsk;
		this.highBid = highBid;
		this.highAsk = highAsk;
		this.lowBid = lowBid;
		this.lowAsk = lowAsk;
		this.closeBid = closeBid;
		this.closeAsk = closeAsk;
		this.volume = volume;
		this.complete = complete;
	}

	public BidAskCandle(Instrument instrument, BrokerID brokerId,
			CandleStickGranularity granularity, Instant time, Double openBid,
			Double openAsk, Double highBid, Double highAsk, Double lowBid,
			Double lowAsk, Double closeBid, Double closeAsk, Integer volume,
			Boolean complete) {
		this(null, instrument, brokerId, granularity, time, openBid, openAsk, highBid, highAsk, lowBid, lowAsk, closeBid, closeAsk, volume, complete);
		this.id = generateId();
	}

	//Ugly thing to use our own generated id
	private String generateId() {
		return new StringBuilder().append(instrument.name()).append("|").append(granularity.name()).append("|").append(time.toString()).toString();
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((granularity == null) ? 0 : granularity.hashCode());
		result = prime * result
				+ ((instrument == null) ? 0 : instrument.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BidAskCandle other = (BidAskCandle) obj;
		if (granularity != other.granularity)
			return false;
		if (instrument != other.instrument)
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BidAskCandle [id=").append(getId()).append(", instrument=")
				.append(instrument).append(", brokerId=").append(brokerId)
				.append(", granularity=").append(granularity).append(", time=")
				.append(time).append(", openBid=").append(openBid)
				.append(", openAsk=").append(openAsk).append(", highBid=")
				.append(highBid).append(", highAsk=").append(highAsk)
				.append(", lowBid=").append(lowBid).append(", lowAsk=")
				.append(lowAsk).append(", closeBid=").append(closeBid)
				.append(", closeAsk=").append(closeAsk).append(", volume=")
				.append(volume).append(", complete=").append(complete)
				.append("]");
		return builder.toString();
	}

}
