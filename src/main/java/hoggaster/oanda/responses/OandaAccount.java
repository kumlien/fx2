package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.BrokerDepot;
import scala.Int;


public class OandaAccount {


    public final String accountId;
    public final String accountName;
    public final String accountCurrency;
    public final String marginRate;
    public final Double balance;
    public final Double unrealizedPl;
    public final Double realizedPl;
    public final Double marginUsed;
    public final Double marginAvail;
    public final Integer openTrades;
    public final Integer openOrders;

    @JsonCreator
    public OandaAccount(@JsonProperty(value = "accountId") String accountId, @JsonProperty(value = "accountName") String accountName, @JsonProperty(value = "accountCurrency") String accountCurrency, @JsonProperty(value = "marginRate") String marginRate,
                        @JsonProperty(value = "balance") Double balance, @JsonProperty(value = "unrealizedPl") Double unrealizedPl, @JsonProperty(value = "realizedPl") Double realizedPl, @JsonProperty(value = "marginUsed") Double marginUsed,
                        @JsonProperty(value = "marginAvail") Double marginAvail, @JsonProperty(value = "openTrades") Integer openTrades, @JsonProperty(value = "openOrders") Integer openOrders) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountCurrency = accountCurrency;
        this.marginRate = marginRate;
        this.balance = balance;
        this.unrealizedPl = unrealizedPl;
        this.realizedPl = realizedPl;
        this.marginUsed = marginUsed;
        this.marginAvail = marginAvail;
        this.openTrades = openTrades;
        this.openOrders = openOrders;
    }

    public final BrokerDepot toBrokerDepot() {
        return new BrokerDepot(accountId, accountName, accountCurrency, marginRate, balance, unrealizedPl, realizedPl, marginUsed, marginAvail, openTrades, openOrders);
    }

    @Override
    public String toString() {
        return "OandaAccount{" +
                "accountId='" + accountId + '\'' +
                ", accountName='" + accountName + '\'' +
                ", accountCurrency='" + accountCurrency + '\'' +
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

