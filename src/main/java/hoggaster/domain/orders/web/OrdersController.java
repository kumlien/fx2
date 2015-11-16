package hoggaster.domain.orders.web;

import com.google.common.base.Preconditions;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.depot.DbDepot;
import hoggaster.domain.depot.Depot;
import hoggaster.domain.depot.DepotImpl;
import hoggaster.domain.depot.DepotService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.domain.orders.OrderResponse;
import hoggaster.domain.orders.OrderService;
import hoggaster.domain.prices.Price;
import hoggaster.domain.prices.PriceService;
import hoggaster.domain.trades.TradeService;
import hoggaster.oanda.responses.OandaOrderResponse;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

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

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation("Send a new order request to oanda using the specified depot")
    public OandaOrderResponse placeOrder(@RequestBody OrderRequest request) {
        LOG.info("Sending order to broker: {}", request);

        OandaOrderResponse result = orderService.sendOrder(request);
        LOG.info("Got response: {}", result);
        return result;

    }

    @RequestMapping(value = "detailed", method = RequestMethod.POST)
    @ApiOperation("Send a new order request to oanda." )
    public OrderResponse placeOrderNew(@RequestBody WebOrderRequest request) {
        Preconditions.checkArgument(StringUtils.hasText(request.depotId), "The provided depotId doesn't contain any text");
        DbDepot dbDepot = depotService.findDepotById(request.depotId);
        Preconditions.checkArgument(dbDepot != null, "No depot found with id " + request.depotId);
        Depot depot = new DepotImpl(request.depotId, orderService, depotService, priceService, tradeService);
        Price p = new Price(request.currencyPair, null, request.price, Instant.now(), Broker.OANDA);
        return depot.buy(request.currencyPair, request.partOfMargin, p, null);
    }
}
