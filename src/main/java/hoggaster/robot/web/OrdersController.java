package hoggaster.robot.web;

import hoggaster.domain.Broker;
import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.responses.OandaOrderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("orders")
public class OrdersController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);
	
	private final Broker oandaApi;
	

	@Autowired
	public OrdersController(Broker oandaApi) {
		this.oandaApi = oandaApi;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public OandaOrderResponse placeOrder(@RequestBody OrderRequest request) {
		LOG.info("Sending order to broker: {}", request);
		OandaOrderResponse result = oandaApi.sendOrderToBroker(request);
		LOG.info("Got response: {}", result);
		return result;
	}

	
	
}
