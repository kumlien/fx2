package hoggaster.domain.orders;

import hoggaster.oanda.responses.OandaOrderResponse;

/**
 * Interface for order service, implemented by broker connections.
 */
public interface OrderService {

    //TODO remove Oanda
    OandaOrderResponse sendOrder(OrderRequest request);
}
