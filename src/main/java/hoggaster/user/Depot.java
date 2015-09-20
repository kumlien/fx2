package hoggaster.user;

import com.google.common.collect.Sets;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.transaction.Transaction;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Set;

@Document
public class Depot {


    @Id
    private String id;

    private final String name;

    //The name used by the broker for this depot
    private final String brokerDepotName;

    private final Broker broker;

    private final BigDecimal marginRate;

    /*
     * The id in the broker system for this depot.
     */
    private final String brokerId;

    private Set<InstrumentOwnership> ownerships = Sets.newHashSet();

    private Set<Transaction> transactions = Sets.newHashSet();

    private BigDecimal balance;

    private String currency;


    @PersistenceConstructor
    public Depot(String id, String name, Broker broker, Set<InstrumentOwnership> ownerships, Set<Transaction> transactions, String brokerId, BigDecimal balance, BigDecimal marginRate, String currency, String brokerDepotName) {
        this.id = id;
        this.name = name;
        this.broker = broker;
        this.marginRate = marginRate;
        this.ownerships = ownerships;
        this.brokerId = brokerId;
        this.transactions = transactions;
        this.balance = balance;
        this.currency = currency;
        this.brokerDepotName = brokerDepotName;
    }

    /**
     * Create a new Depot.
     *
     * @param name Our internal name for this depot
     * @param broker The broker to which this depot is connected.
     * @param brokerDepotName The name of this depot/account on the broker side
     * @param brokerId The id of this depot on the broker side
     * @param marginRate The margin rate for this depot
     *
     */
    public Depot(String name, Broker broker, String brokerDepotName, String brokerId, BigDecimal marginRate) {
        this.name = name;
        this.broker = broker;
        this.brokerId = brokerId;
        this.balance = new BigDecimal(0l);
        this.marginRate = marginRate;
        this.brokerDepotName = brokerDepotName;
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

    public String getName() {
        return name;
    }

    public String getBrokerId() {
        return brokerId;
    }

    /**
     * TODO Should be the responsibility of the depot to carry out the trad (using services of course)
     *
     * Signal a that we bought something
     * Add to set of {@link InstrumentOwnership} if not already present
     *
     * @param instrument
     * @param quantity
     * @param pricePerShare
     */
    public void bought(Instrument instrument, BigDecimal quantity, BigDecimal pricePerShare) {
        synchronized (ownerships) {
            InstrumentOwnership io = findByInstrument(instrument);
            if (io == null) {
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


    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getMarginRate() {
        return marginRate;
    }

    public String getBrokerDepotName() {
        return brokerDepotName;
    }

    public String getId() {
        return id;
    }

}
