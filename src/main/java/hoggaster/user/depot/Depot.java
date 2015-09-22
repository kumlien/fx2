package hoggaster.user.depot;

import com.google.common.collect.Sets;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.transaction.Transaction;
import hoggaster.user.InstrumentOwnership;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Document
public class Depot {

    @Id
    private String id;

    private  BigDecimal unrealizedPl;
    private  BigDecimal realizedPl;
    private  BigDecimal marginUsed;
    private  BigDecimal marginAvailable;
    private  Integer openTrades;
    private  Integer openOrders;

    public final String userId;

    public final String name;

    //The name used by the broker for this depot
    public final String brokerDepotName;

    public final Broker broker;

    public final BigDecimal marginRate;

    /*
     * The id in the broker system for this depot.
     */
    public final String brokerId;

    private Set<InstrumentOwnership> ownerships = Sets.newHashSet();

    private Set<Transaction> transactions = Sets.newHashSet();

    private BigDecimal balance;

    public final String currency;

    private Instant lastSynchronizedWithBroker;


    @PersistenceConstructor
    public Depot(String id, String userId, String name, Broker broker, Set<InstrumentOwnership> ownerships, Set<Transaction> transactions, String brokerId, BigDecimal balance, BigDecimal marginRate, String currency, String brokerDepotName, BigDecimal unrealizedPl, BigDecimal realizedPl,
                 BigDecimal marginUsed, BigDecimal marginAvailable, Integer openTrades, Integer openOrders, Instant lastSynchronizedWithBroker) {
        this(userId, name, broker, brokerDepotName, brokerId, marginRate, currency, balance, unrealizedPl, realizedPl, marginUsed, marginAvailable, openTrades, openOrders, lastSynchronizedWithBroker);
        this.id = id;
        this.transactions = transactions;
        this.ownerships = ownerships;
    }

    /**
     * Create a new Depot.
     *
     * @param userId The id of the user owning this depot
     * @param name Our internal name for this depot
     * @param broker The broker to which this depot is connected.
     * @param brokerDepotName The name of this depot/account on the broker side
     * @param brokerId The id of this depot on the broker side
     * @param marginRate The margin rate for this depot
     * @param currency The base currency for this depot
     *
     */
    public Depot(String userId, String name, Broker broker, String brokerDepotName, String brokerId, BigDecimal marginRate, String currency, BigDecimal balance, BigDecimal unrealizedPl, BigDecimal realizedPl, BigDecimal marginUsed, BigDecimal marginAvailable, Integer openTrades, Integer openOrders, Instant lastSynchronizedWithBroker) {
        this.userId = userId;
        this.name = name;
        this.broker = broker;
        this.brokerId = brokerId;
        this.balance = balance;
        this.unrealizedPl = unrealizedPl;
        this.realizedPl = realizedPl;
        this.marginUsed = marginUsed;
        this.marginAvailable = marginAvailable;
        this.openTrades = openTrades;
        this.openOrders = openOrders;
        this.marginRate = marginRate;
        this.brokerDepotName = brokerDepotName;
        this.currency = currency;
        this.lastSynchronizedWithBroker = lastSynchronizedWithBroker;
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

    public BigDecimal getUnrealizedPl() {
        return unrealizedPl;
    }

    public void setUnrealizedPl(BigDecimal unrealizedPl) {
        this.unrealizedPl = unrealizedPl;
    }

    public BigDecimal getRealizedPl() {
        return realizedPl;
    }

    public void setRealizedPl(BigDecimal realizedPl) {
        this.realizedPl = realizedPl;
    }

    public BigDecimal getMarginUsed() {
        return marginUsed;
    }

    public void setMarginUsed(BigDecimal marginUsed) {
        this.marginUsed = marginUsed;
    }

    public BigDecimal getMarginAvailable() {
        return marginAvailable;
    }

    public void setMarginAvailable(BigDecimal marginAvailable) {
        this.marginAvailable = marginAvailable;
    }

    public Integer getOpenTrades() {
        return openTrades;
    }

    public void setOpenTrades(Integer openTrades) {
        this.openTrades = openTrades;
    }

    public Integer getOpenOrders() {
        return openOrders;
    }

    public void setOpenOrders(Integer openOrders) {
        this.openOrders = openOrders;
    }
}
