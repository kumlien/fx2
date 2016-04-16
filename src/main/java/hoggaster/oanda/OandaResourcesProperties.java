package hoggaster.oanda;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oanda.resources", exceptionIfInvalid = true, locations = "file:/data/fx2/config/application.yml")
public class OandaResourcesProperties {

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda accounts endpoint")
    private String accounts;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda account endpoint")
    private String account;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda instruments endpoint")
    private String instruments;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda candles endpoint")
    private String candles;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda prices endpoint")
    private String prices;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda streaming prices endpoint")
    private String streamingPrices;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda orders endpoint")
    private String orders;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda positions endpoint")
    private String positions;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda position endpoint")
    private String position;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda trades endpoint")
    private String trades;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda trade endpoint")
    private String trade;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for oanda streaming events endpoint")
    private String streamingEvents;

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getInstruments() {
        return instruments;
    }

    public void setInstruments(String instruments) {
        this.instruments = instruments;
    }

    public String getCandles() {
        return candles;
    }

    public void setCandles(String candles) {
        this.candles = candles;
    }

    public String getPrices() {
        return prices;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTrades() {
        return trades;
    }

    public void setTrades(String trades) {
        this.trades = trades;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStreamingPrices() {
        return streamingPrices;
    }

    public void setStreamingPrices(String streamingPrices) {
        this.streamingPrices = streamingPrices;
    }

    public String getStreamingEvents() {
        return streamingEvents;
    }

    public void setStreamingEvents(String streamingEvents) {
        this.streamingEvents = streamingEvents;
    }
}
