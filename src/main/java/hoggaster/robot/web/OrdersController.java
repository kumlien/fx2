package hoggaster.robot.web;

import hoggaster.domain.BrokerConnection;
import hoggaster.domain.OrderService;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.responses.OandaOrderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("orders")
public class OrdersController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);
	
	private final BrokerConnection oandaApi;
	
	private final OrderService oandaOrderService;
	

	@Autowired
	public OrdersController(@Qualifier("OandaBrokerConnection") BrokerConnection oandaApi, @Qualifier("OandaOrderService") OrderService oandaOrderService) {
		this.oandaApi = oandaApi;
		this.oandaOrderService = oandaOrderService;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public OandaOrderResponse placeOrder(@RequestBody OrderRequest request) {
		LOG.info("Sending order to broker: {}", request);
		OandaOrderResponse result = oandaOrderService.sendOrder(request);
		LOG.info("Got response: {}", result);
		return result;
	}

	
	
}
