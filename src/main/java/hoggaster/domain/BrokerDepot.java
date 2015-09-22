package hoggaster.domain;

/**
 * Generic depot on the broker side.
 * Right now it's a copy of the OandaAccount, until we add support for other brokers.
 *
 * Created by svante2 on 15-09-21.
 */
public class BrokerDepot {

    public final String id;
    public final String name;
    public final String currency;
    public final String marginRate;
    public final Double balance;
    public final Double unrealizedPl;
    public final Double realizedPl;
    public final Double marginUsed;
    public final Double marginAvail;
    public final Integer openTrades;
    public final Integer openOrders;

    public BrokerDepot(String id, String name, String currency, String marginRate, Double balance, Double unrealizedPl, Double realizedPl, Double marginUsed, Double marginAvail, Integer openTrades, Integer openOrders) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.marginRate = marginRate;
        this.balance = balance;
        this.unrealizedPl = unrealizedPl;
        this.realizedPl = realizedPl;
        this.marginUsed = marginUsed;
        this.marginAvail = marginAvail;
        this.openTrades = openTrades;
        this.openOrders = openOrders;
    }

    @Override
    public String toString() {
        return "BrokerDepot{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", currency='" + currency + '\'' +
                ", marginRate='" + marginRate + '\'' +
                ", balance=" + balance +
                ", unrealizedPl=" + unrealizedPl +
                ", realizedPl=" + realizedPl +
                ", marginUsed=" + marginUsed +
                ", marginAvail=" + marginAvail +
                ", openTrades=" + openTrades +
                ", openOrders=" + openOrders +
                '}';
    }
}
