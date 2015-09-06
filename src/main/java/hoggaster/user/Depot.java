package hoggaster.user;

import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.transaction.Transaction;

import java.math.BigDecimal;
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
	
	private Set<Transaction> transactions = Sets.newHashSet();
	
	private BigDecimal balance;
	

	@PersistenceConstructor
	public Depot(String id, Broker broker,Set<InstrumentOwnership> ownerships, Set<Transaction> transactions, String brokerId, BigDecimal balance) {
		this.id = id;
		this.broker = broker;
		this.ownerships = ownerships;
		this.brokerId = brokerId;
		this.transactions = transactions;
	}

	/**
	 * Create a new Depot.
	 * 
	 * @param broker The broker to which this depot is connected.
	 */
	public Depot(Broker broker, String brokerId) {
		this.broker = broker;
		this.brokerId = brokerId;
		this.balance = new BigDecimal(0l);
	}

	public boolean ownThisInstrument(Instrument instrument) {
		return findByInstrument(instrument) != null;
	}
	
	private InstrumentOwnership findByInstrument(Instrument instrument) {
	    return ownerships.stream().filter(io -> io.getInstrument() == instrument).findFirst().orElse(null);
	}

	public Broker getBroker() {
		return broker;
	}


	public String getBrokerId() {
		return brokerId;
	}

	/**
	 * Signal a that we bought something
	 * Add to set of {@link InstrumentOwnership} if not already present
	 * @param instrument
	 * @param quantity
	 * @param totalPrice
	 */
	public void bought(Instrument instrument, BigDecimal quantity, BigDecimal pricePerShare) {
	    synchronized (ownerships) {
		InstrumentOwnership io = findByInstrument(instrument);
		if(io == null) {
		    io = new InstrumentOwnership(instrument);
		}
		io.add(quantity, pricePerShare);
	    }
	}

	public void sold() {
	    // TODO Auto-generated method stub
	    
	}

	public Set<Transaction> getTransactions() {
	    return transactions;
	}

	public void setTransactions(Set<Transaction> transactions) {
	    this.transactions = transactions;
	}


	
	

}
