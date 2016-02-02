package hoggaster.domain.orders.web;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotImpl;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.CreateOrderResponse;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.TradeService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Objects;

@RestController()
@RequestMapping("orders")
public class OrdersController {

    private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);

    private final DepotService depotService;
    private final BrokerConnection orderService;
    private final PriceService priceService;
    private final TradeService tradeService;

    @Autowired
    public OrdersController(@Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.orderService = brokerConnection;
        this.depotService = depotService;
        this.priceService = priceService;
        this.tradeService = tradeService;
    }


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Send a new order request to oanda for the specified depotId" )
    public CreateOrderResponse placeOrderNew(@RequestParam("depotId") String depotId, @RequestParam("instrument")CurrencyPair currencyPair, @RequestParam("side") OrderSide side, @RequestParam("partOfMargin")BigDecimal partOfMargin) {
        Preconditions.checkArgument(StringUtils.hasText(depotId), "The provided depotId doesn't contain any text");
        DbDepot dbDepot = depotService.findDepotById(depotId);
        Preconditions.checkArgument(dbDepot != null, "No depots found with id " + depotId);
        Depot depot = new DepotImpl(depotId, orderService, depotService, priceService, tradeService);
        return Objects.requireNonNull(depot.openTrade(currencyPair, side, partOfMargin, null, "web-request"), "Ooops, the order was not sent for some reason, check the logs...");
    }
}
