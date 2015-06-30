package hoggaster.user;

import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.collect.Sets;

@Document
public class Depot {
	
	@Id
	private String id;
	
	private final Broker broker;
	
	/*
	 * The id in the broker system for this depot.
	 */
	private final String brokerId;
	
	private Set<InstrumentOwnership> ownerships = Sets.newHashSet();
	

	@PersistenceConstructor
	public Depot(String id, Broker broker,Set<InstrumentOwnership> ownerships, String brokerId) {
		this.id = id;
		this.broker = broker;
		this.ownerships = ownerships;
		this.brokerId = brokerId;
	}

	/**
	 * Create a new Depot.
	 * 
	 * @param broker The broker to which this depot is connected.
	 */
	public Depot(Broker broker, String brokerId) {
		this.broker = broker;
		this.brokerId = brokerId;
	}

	public boolean ownThisInstrument(Instrument instrument) {
		return ownerships.stream().filter(io -> io.getInstrument() == instrument).findFirst().orElse(null) != null;
	}

	public Broker getBroker() {
		return broker;
	}


	public String getBrokerId() {
		return brokerId;
	}


	
	

}
