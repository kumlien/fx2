package hoggaster.domain.depots;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.positions.Position;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.trades.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.Optional;

@Document(collection = "depots")
public class DbDepot {

    private static final Logger LOG = LoggerFactory.getLogger(DbDepot.class);

    @Id
    private String id;

    /**
     * The current profit or loss total for all your open trades.
     * If you were to close all of your open trades at this exact point in time,
     * this amount would be "realized" and added to the Realized P&L.
     */
    private BigDecimal unrealizedPl;

    /**
     * The amount of profit or loss you have incurred with your trading activity to date.
     * This value changes when you realize profits or losses on your open positions
     */
    private BigDecimal realizedPl;

    /**
     * The amount of your account balance and Unrealized P&L that is reserved for margin.
     * This amount is equal to the Position Value multiplied by your margin ratio.
     * The margin ratio is the inverse of leverage; for example, 50:1 leverage equals a 0.02 margin ratio.
     * Look here for oanda margin rules: http://fxtrade.oanda.com/help/policies/margin-rules
     */
    private BigDecimal marginUsed;

    /**
     * The amount of your Balance and Unrealized P&L available as margin for new trading transactions.
     * It is equal to your Net Asset Value minus Margin Used.
     */
    private BigDecimal marginAvailable;
    private Integer numberOfOpenTrades;
    private Integer numberOfOpenOrders;

    public final String userId;

    public Type getType() {
        return type;
    }

    public final Type type;

    public final String name;

    //The name used by the broker for this dbDepot
    private String brokerDepotName;

    public final Broker broker;

    public BigDecimal marginRate;

    /**
     * The id in the broker system for this dbDepot.
     */
    public final String brokerId;

    /**
     * A list of open positions
     */
    private Set<Position> positions = Sets.newHashSet();


    /**
     * A list of open trades
     */
    private Set<Trade> openTrades = Sets.newHashSet();

    private Set<RobotDefinition> robotDefinitions = Sets.newHashSet();


    /**
     * The amount of cash in your account.
     * Your balance changes when you realize a profit or loss on your positions, earn/pay interest, or deposit/withdraw funds.
     * This value does not change with the current exchange rate on your open positions.
     */
    private BigDecimal balance;

    public Currency currency;

    private Boolean lastSyncOk;

    private Instant lastSynchronizedWithBroker;


    /**
     *
     * @param id
     * @param userId
     * @param name
     * @param broker
     * @param positions
     * @param brokerId
     * @param balance
     * @param marginRate
     * @param currency
     * @param brokerDepotName
     * @param unrealizedPl
     * @param realizedPl
     * @param marginUsed
     * @param marginAvailable
     * @param numberOfOpenTrades
     * @param numberOfOpenOrders
     * @param lastSynchronizedWithBroker
     * @param lastSyncOk
     * @param type
     */
    @PersistenceConstructor
    public DbDepot(String id, String userId, String name, Broker broker, Set<Position> positions, Set<Trade> openTrades, String brokerId, BigDecimal balance, BigDecimal marginRate, Currency currency, String brokerDepotName, BigDecimal unrealizedPl, BigDecimal realizedPl,
                   BigDecimal marginUsed, BigDecimal marginAvailable, Integer numberOfOpenTrades, Integer numberOfOpenOrders, Instant lastSynchronizedWithBroker, Boolean lastSyncOk, Type type, Set<RobotDefinition> robotDefinitions) {
        this(userId, name, broker, brokerDepotName, brokerId, marginRate, currency, balance, unrealizedPl, realizedPl, marginUsed, marginAvailable, numberOfOpenTrades, numberOfOpenOrders, lastSynchronizedWithBroker, lastSyncOk, type);
        this.id = id;
        this.positions = positions;
        this.numberOfOpenTrades = numberOfOpenTrades;
        this.openTrades = openTrades;
        this.robotDefinitions = robotDefinitions;
    }


    /**
     *
     * Create/connect a new depot. Used from {@link DepotService}
     *
     * @param userId
     * @param name
     * @param broker
     * @param brokerDepotName
     * @param brokerId
     * @param marginRate
     * @param currency
     * @param balance
     * @param unrealizedPl
     * @param realizedPl
     * @param marginUsed
     * @param marginAvailable
     * @param numberOfOpenTrades
     * @param numberOfOpenOrders
     * @param lastSynchronizedWithBroker
     * @param lastSyncOk
     * @param type
     */
    public DbDepot(String userId, String name, Broker broker, String brokerDepotName, String brokerId, BigDecimal marginRate, Currency currency, BigDecimal balance, BigDecimal unrealizedPl, BigDecimal realizedPl, BigDecimal marginUsed, BigDecimal marginAvailable, Integer numberOfOpenTrades, Integer numberOfOpenOrders, Instant lastSynchronizedWithBroker, Boolean lastSyncOk, Type type) {
        this.userId = userId;
        this.name = name;
        this.broker = broker;
        this.brokerId = brokerId;
        this.balance = balance;
        this.unrealizedPl = unrealizedPl;
        this.realizedPl = realizedPl;
        this.marginUsed = marginUsed;
        this.marginAvailable = marginAvailable;
        this.numberOfOpenTrades = numberOfOpenTrades;
        this.numberOfOpenOrders = numberOfOpenOrders;
        this.marginRate = marginRate;
        this.brokerDepotName = brokerDepotName;
        this.currency = currency;
        this.lastSynchronizedWithBroker = lastSynchronizedWithBroker;
        this.lastSyncOk = lastSyncOk;
        this.type = type;
    }

    public boolean hasOpenPositionForInstrument(CurrencyPair currencyPair) {
        return getPositionByInstrumentInternal(currencyPair) != null;
    }

    /**
     * Get (a copy of) the position for the specified CurrencyPair
     *
     * @param currencyPair
     * @return A Position or null if not found.
     */
    public Position getPositionByInstrument(CurrencyPair currencyPair) {
        Preconditions.checkArgument(currencyPair != null, "The currencyParameter must be specfied");
        final Position original = getPositionByInstrumentInternal(currencyPair);
        if (original == null) return null;
        return new Position(original);
    }

    /**
     * Get a read only view of the current positions.
     *
     * @return The current positions.
     */
    public Collection<Position> getPositions() {
        return Collections.unmodifiableCollection(positions);
    }

    private Position getPositionByInstrumentInternal(CurrencyPair currencyPair) {
        return positions.stream().filter(io -> io.getCurrencyPair() == currencyPair).findFirst().orElse(null);
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
     * Signal that we tradeOpened something
     * Add to set of {@link Position}s if not already present
     *
     * @param trade The new trade.
     */
    public void tradeOpened(Trade trade) {
        synchronized (positions) {
            Position position = getPositionByInstrumentInternal(trade.instrument);
            if (position == null) {
                position = new Position(trade.instrument, trade.side, trade.units, trade.openPrice);
                positions.add(position);
            } else {
                position.newTrade(trade.units, trade.openPrice, trade.side);
            }
        }
    }


    /**
     * Update our values with the values from the BrokerDepot
     *
     * @param brokerDepot
     * @param positions
     * @return true if any fields were changed
     */
    public boolean updateWithValuesFrom(BrokerDepot brokerDepot, Set<Position> positions, Set<Trade> openTrades) {
        boolean changed = false;
        if (balance.compareTo(brokerDepot.balance) != 0) {
            LOG.debug("Balance updated with new value for dbDepot {}: {} -> {}", id, balance, brokerDepot.balance);
            balance = brokerDepot.balance;
            changed = true;
        }

        if (marginAvailable == null || marginAvailable.compareTo(brokerDepot.marginAvail) != 0) {
            LOG.debug("Available margin updated with new value for dbDepot {}: {} -> {}", id, marginAvailable, brokerDepot.marginAvail);
            marginAvailable = brokerDepot.marginAvail;
            changed = true;
        }

        if (currency == null || !currency.equals(brokerDepot.currency)) {
            LOG.warn("Currency updated with new value for dbDepot {}: {} -> {}", id, currency, brokerDepot.currency);
            currency = brokerDepot.currency;
            changed = true;
        }

        if (marginRate == null || marginRate.compareTo(brokerDepot.marginRate) != 0) {
            LOG.warn("Margin rate updated with new value for dbDepot {}: {} -> {}", id, marginRate, brokerDepot.marginRate);
            marginRate = brokerDepot.marginRate;
            changed = true;
        }

        if (marginUsed == null || marginUsed.compareTo(brokerDepot.marginUsed) != 0) {
            LOG.debug("Margin used updated with new value for dbDepot {}: {} -> {}", id, marginUsed, brokerDepot.marginUsed);
            marginUsed = brokerDepot.marginUsed;
            changed = true;
        }

        if (numberOfOpenOrders == null || numberOfOpenOrders.compareTo(brokerDepot.openOrders) != 0) {
            LOG.info("Open orders updated with new value for dbDepot {}: {} -> {}", id, numberOfOpenOrders, brokerDepot.openOrders);
            numberOfOpenOrders = brokerDepot.openOrders;
            changed = true;
        }

        if (numberOfOpenTrades == null || numberOfOpenTrades.compareTo(brokerDepot.openTrades) != 0) {
            LOG.info("Open trades updated with new value for dbDepot {}: {} -> {}", id, numberOfOpenTrades, brokerDepot.openTrades);
            numberOfOpenTrades = brokerDepot.openTrades;
            changed = true;
        }

        if (realizedPl == null || realizedPl.compareTo(brokerDepot.realizedPl) != 0) {
            LOG.debug("Realized profit/loss updated with new value for dbDepot {}: {} -> {}", id, realizedPl, brokerDepot.realizedPl);
            realizedPl = brokerDepot.realizedPl;
            changed = true;
        }

        if (unrealizedPl == null || unrealizedPl.compareTo(brokerDepot.unrealizedPl) != 0) {
            LOG.debug("Unrealized profit/loss updated with new value for dbDepot {}: {} -> {}", id, unrealizedPl, brokerDepot.unrealizedPl);
            unrealizedPl = brokerDepot.unrealizedPl;
            changed = true;
        }

        if (Strings.isNullOrEmpty(brokerDepotName) || !brokerDepotName.equals(brokerDepot.name)) {
            LOG.debug("Name updated with new value for dbDepot {}: {} -> {}", id, brokerDepotName, brokerDepot.name);
            brokerDepotName = brokerDepot.name;
            changed = true;
        }

        if(this.positions == null) {
            this.positions = new HashSet<>();
        }


        if(this.positions.size() != positions.size() ||
                positions.stream()
                        .filter(newPosition -> !this.positions.contains(newPosition))
                        .count() > 0) {
            changed = true;
            LOG.info("Positions updated with new values for dbDepot {}: {} -> {}", id, this.positions, positions);
            this.positions = positions;
        }


        if(this.openTrades.size() != openTrades.size() ||
                openTrades.stream()
                        .filter(newTrade -> !this.openTrades.contains(newTrade))
                        .count() > 0) {
            changed = true;
            LOG.info("Open trades updated with new values for dbDepot {}: {} -> {}", id, this.openTrades, openTrades);
            this.openTrades = openTrades;
        }

        return changed;
    }


    public Currency getCurrency() {
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

    public Integer getNumberOfOpenTrades() {
        return numberOfOpenTrades;
    }

    public void setNumberOfOpenTrades(Integer numberOfOpenTrades) {
        this.numberOfOpenTrades = numberOfOpenTrades;
    }

    public Integer getNumberOfOpenOrders() {
        return numberOfOpenOrders;
    }

    public void setNumberOfOpenOrders(Integer numberOfOpenOrders) {
        this.numberOfOpenOrders = numberOfOpenOrders;
    }

    public Boolean getLastSyncOk() {
        return lastSyncOk;
    }

    public void setLastSyncOk(Boolean lastSyncOk) {
        this.lastSyncOk = lastSyncOk;
    }

    public Instant getLastSynchronizedWithBroker() {
        return lastSynchronizedWithBroker;
    }

    public void setLastSynchronizedWithBroker(Instant lastSynchronizedWithBroker) {
        this.lastSynchronizedWithBroker = lastSynchronizedWithBroker;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DbDepot{");
        sb.append("id='").append(id).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", broker=").append(broker);
        sb.append('}');
        return sb.toString();
    }

    public void buy(CurrencyPair currencyPair) {
    }

    public Optional<Trade> getOpenTrade(String tradeId) {
        return openTrades.stream().filter(t -> t.getId().equals(tradeId)).findFirst();
    }

    public Collection<Trade> getOpenTrades() {
        return Collections.unmodifiableCollection(openTrades);
    }

    public Collection<RobotDefinition> getRobotDefinitions() {
        if(robotDefinitions == null) {
            robotDefinitions = new HashSet<>();
        }
        return Collections.unmodifiableCollection(robotDefinitions);
    }

    private Collection<RobotDefinition> getRobotDefinitionsInternal() {
        if(robotDefinitions == null) {
            robotDefinitions = new HashSet<>();
        }
        return robotDefinitions;
    }

    public void addRobotDefinition(RobotDefinition robotDefinition) {
        Preconditions.checkArgument(!getRobotDefinitionsInternal().contains(robotDefinition), "There is already a robotDefinition with id " + robotDefinition.getId());
        getRobotDefinitionsInternal().add(robotDefinition);
    }

    public void updateRobotDefinition(RobotDefinition robotDefinition) {
        Preconditions.checkArgument(robotDefinition != null);
        Preconditions.checkArgument(StringUtils.hasText(robotDefinition.getId()));
        Preconditions.checkArgument(getRobotDefinitionsInternal().contains(robotDefinition), "No robot found with id " + robotDefinition.getId());
        synchronized (getRobotDefinitionsInternal()) {
            removeRobotDefinition(robotDefinition.getId());
            addRobotDefinition(robotDefinition);
        }
    }

    public boolean removeRobotDefinition(String id) {
        for(Iterator<RobotDefinition> iter = getRobotDefinitionsInternal().iterator(); iter.hasNext();) {
            RobotDefinition rd = iter.next();
            if(rd.getId().equals(id)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DbDepot dbDepot = (DbDepot) o;
        return com.google.common.base.Objects.equal(id, dbDepot.id);
    }

    @Override public int hashCode() {
        return Objects.hashCode(id);
    }

    public enum Type {
        LIVE, DEMO, SIMULATION
    }
}
