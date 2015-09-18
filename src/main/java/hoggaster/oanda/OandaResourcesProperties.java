package hoggaster.oanda;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oanda.resources")
public class OandaResourcesProperties {

    private String accounts;

    private String instruments;

    private String candles;

    private String prices;

    private String orders;


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

}
