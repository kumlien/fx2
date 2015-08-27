package hoggaster.domain;

import hoggaster.domain.orders.OrderRequest;
import hoggaster.oanda.responses.OandaOrderResponse;

public interface OrderService {
    
    	//TODO remove Oanda
  	OandaOrderResponse sendOrder(OrderRequest request);
}
