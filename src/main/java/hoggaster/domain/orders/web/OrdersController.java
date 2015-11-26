package hoggaster.domain.orders.web;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.Depot;
import hoggaster.domain.depots.DepotImpl;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

@RestController()
@RequestMapping("orders")
public class OrdersController {

    private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);

    private final DepotService depotService;
    private final OrderService orderService;
    private final PriceService priceService;
    private final TradeService tradeService;

    @Autowired
    public OrdersController(@Qualifier("OandaOrderService") OrderService oandaOrderService, DepotService depotService, PriceService priceService, TradeService tradeService) {
        this.orderService = oandaOrderService;
        this.depotService = depotService;
        this.priceService = priceService;
        this.tradeService = tradeService;
    }

    //@RequestMapping(method = RequestMethod.POST)
    //@ApiOperation("Send a new order request to directly to oanda using the specified depotId")
    public OrderResponse placeOrderDirect(@RequestBody OrderRequest request) {
        LOG.info("Sending order to broker: {}", request);

        OrderResponse result = orderService.sendOrder(request);
        LOG.info("Got response: {}", result);
        return result;

    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Send a new order request to oanda for the specified depotId" )
    public OrderResponse placeOrderNew(@RequestParam("depotId") String depotId, @RequestParam("instrument")CurrencyPair currencyPair, @RequestParam("side") OrderSide side, @RequestParam("partOfMargin")BigDecimal partOfMargin) {
        Preconditions.checkArgument(StringUtils.hasText(depotId), "The provided depotId doesn't contain any text");
        DbDepot dbDepot = depotService.findDepotById(depotId);
        Preconditions.checkArgument(dbDepot != null, "No depots found with id " + depotId);
        Depot depot = new DepotImpl(depotId, orderService, depotService, priceService, tradeService);
        return Objects.requireNonNull(depot.sendOrder(currencyPair, side, partOfMargin, null, "web-request"), "Ooops, the order was not sent for some reason, check the logs...");
    }
}
