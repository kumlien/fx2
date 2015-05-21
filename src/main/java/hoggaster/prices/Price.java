package hoggaster.prices;

import hoggaster.BrokerID;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaPrice;

import java.time.Instant;
import java.util.Date;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Price {
	
	private Long id;
	public final Instrument instrument;
	public final Double bid;
	public final Double ask;
	public final Instant time;
	public final BrokerID broker;
	
	@PersistenceConstructor
	public Price(Long id, Instrument instrument, Double bid, Double ask, Instant time, BrokerID broker) {
		this.id = id;
		this.instrument = instrument;
		this.bid = bid;
		this.ask = ask;
		this.time = time;
		this.broker = broker;
	}

	public Price(Instrument instrument, Double bid, Double ask, Instant time, BrokerID broker) {
		this.instrument = instrument;
		this.bid = bid;
		this.ask = ask;
		this.time = time;
		this.broker = broker;
	}

	public Price(OandaPrice p) {
		this.instrument = Instrument.valueOf(p.instrument);
		this.bid = p.bid;
		this.ask = p.ask;
		this.time = p.time.toInstant();
		this.broker = BrokerID.OANDA;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Price [id=").append(id).append(", instrument=")
				.append(instrument).append(", bid=").append(bid)
				.append(", ask=").append(ask).append(", time=").append(time)
				.append(", broker=").append(broker).append("]");
		return builder.toString();
	}


}
