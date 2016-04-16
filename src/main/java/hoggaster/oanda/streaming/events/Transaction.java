package hoggaster.oanda.streaming.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Created by svante2 on 2016-04-15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    @JsonProperty("id")
    public Integer id;
    @JsonProperty("accountId")
    public Integer accountId;
    @JsonProperty("time")
    public Date time;
    @JsonProperty("type")
    public TransactionType type;
    @JsonProperty("instrument")
    public CurrencyPair instrument;
    @JsonProperty("units")
    public Integer units;
    @JsonProperty("side")
    public OrderSide side;
    @JsonProperty("price")
    public BigDecimal price;
    @JsonProperty("lowerBound")
    public BigDecimal lowerBound;
    @JsonProperty("upperBound")
    public BigDecimal upperBound;
    @JsonProperty("takeProfitPrice")
    public BigDecimal takeProfitPrice;
    @JsonProperty("stopLossPrice")
    public BigDecimal stopLossPrice;
    @JsonProperty("trailingStopLossDistance")
    public BigDecimal trailingStopLossDistance;
    @JsonProperty("pl")
    public BigDecimal pl;
    @JsonProperty("interest")
    public BigDecimal interest;
    @JsonProperty("accountBalance")
    public BigDecimal accountBalance;
    @JsonProperty("tradeId")
    public Integer tradeId;
    @JsonProperty("orderId")
    public Integer orderId;
    @JsonProperty("tradeOpened")
    public Object tradeOpened; //TODO
    @JsonProperty("tradeReduced")
    public Object tradeReduced; //TODO



}