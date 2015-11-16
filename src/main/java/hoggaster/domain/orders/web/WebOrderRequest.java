package hoggaster.domain.orders.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import hoggaster.domain.CurrencyPair;

import java.math.BigDecimal;

/**
 * Created by svante2 on 2015-11-16.
 */
public class WebOrderRequest {

    public final String depotId;

    public final CurrencyPair currencyPair;

    public final BigDecimal partOfMargin;

    public final BigDecimal price;

    @JsonCreator
    public WebOrderRequest(@JsonProperty("depotId") String depotId, @JsonProperty("currencyPair") CurrencyPair currencyPair, @JsonProperty("partOfMargin") BigDecimal partOfMargin, @JsonProperty("price") BigDecimal price) {
        this.depotId = depotId;
        this.currencyPair = currencyPair;
        this.partOfMargin = partOfMargin;
        this.price = price;
    }
}
